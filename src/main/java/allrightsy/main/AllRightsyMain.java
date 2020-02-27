package allrightsy.main;

import allrightsy.Artifact;
import allrightsy.ArtifactType;
import allrightsy.License;
import com.fasterxml.jackson.databind.JsonNode;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.io.FilesystemWalker;
import org.cobbzilla.util.main.BaseMain;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.daemon.ZillaRuntime.shortError;
import static org.cobbzilla.util.http.HttpSchemes.SCHEME_HTTP;
import static org.cobbzilla.util.http.HttpSchemes.SCHEME_HTTPS;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.json.JsonUtil.fromJsonOrDie;
import static org.cobbzilla.util.json.JsonUtil.json;
import static org.cobbzilla.util.system.CommandShell.execScript;

public class AllRightsyMain extends BaseMain<AllRightsyOptions> {

    public static void main (String[] args) { main(AllRightsyMain.class, args); }

    @Override protected void run() throws Exception {
        final AllRightsyOptions opts = getOptions();

        final File outFile = opts.getOutFile();
        if (!abs(outFile).endsWith(".json")) {
            die("outfile does not end with .json");
            return;
        }

        final List<Artifact> artifacts = new ArrayList<>();
        for (File f : opts.getPackageFiles()) {
            if (f.getName().equals("pom.xml")) {
                artifacts.addAll(processPom(f));

            } else if (f.getName().equals("node_modules") && f.isDirectory()) {
                new FilesystemWalker()
                        .withDir(f)
                        .withVisitor(file -> {
                            if (!file.getName().equals("package.json")) return;
                            artifacts.addAll(processPackageJson(file));
                        })
                        .walk();

            } else if (f.getName().endsWith(".json")) {
                for (Artifact artifact : fromJsonOrDie(f, Artifact[].class)) {
                    for (License license : artifact.getLicenses()) {
                        if (empty(license.getText()) && !empty(license.getUrl())) {
                            final String licenseContents = url2stringFollowRedirects(license.getUrl());
                            if (!empty(licenseContents)) {
                                license.setText(licenseContents);
                            } else {
                                license.setText(license.getUrl());
                            }
                        }
                    }
                    artifacts.add(artifact);
                }
            } else {
                err("Unrecognized pack, skipping:"+abs(f));
            }
        }

        FileUtil.toFile(outFile, json(artifacts));
    }

    private List<Artifact> processPom(File pom) {
        final List<Artifact> artifacts = new ArrayList<>();
        final String pomDir = abs(pom.getParentFile());
        execScript("cd "+ pomDir +" && mvn license:aggregate-download-licenses");
        final File licenseXml = new File(pomDir+"/target/generated-resources/licenses.xml");
        if (!licenseXml.exists()) return die("processPom: licenseXml not found: "+abs(licenseXml));

        final Document document;
        try {
            document = DocumentHelper.parseText(FileUtil.toStringOrDie(licenseXml));
        } catch (DocumentException e) {
            err("error parsing "+abs(licenseXml)+": "+shortError(e));
            return artifacts;
        }
        for (Element dep : document.getRootElement()
                .element("dependencies")
                .elements("dependency")) {
            final String groupId = dep.elementText("groupId");
            final String artifactId = dep.elementText("artifactId");
            for (Element lic : dep.element("licenses").elements("license")) {
                final String name = lic.elementText("name");
                final String url = lic.elementText("url");
                final String comments = lic.elementText("comments");
                String licenseContent = null;
                if (!empty(url)) {
                    try {
                        licenseContent = url2stringFollowRedirects(url);
                        if (emptyLicense(licenseContent)) {
                            licenseContent = url2stringFollowRedirects(url+"/");
                            if (emptyLicense(licenseContent) && url.startsWith(SCHEME_HTTP)) {
                                licenseContent = url2stringFollowRedirects(SCHEME_HTTPS+url.substring(SCHEME_HTTP.length()));
                                if (emptyLicense(licenseContent)) {
                                    licenseContent = url2stringFollowRedirects(SCHEME_HTTPS+url.substring(SCHEME_HTTP.length())+"/");
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (!empty(comments)) {
                            licenseContent = comments;
                        }
                    }
                    if (emptyLicense(licenseContent)) licenseContent = url;
                }
                if (emptyLicense(licenseContent)) {
                    licenseContent = spdxLicenseContent(normalizeSpdxLicense(name));
                    if (emptyLicense(licenseContent)) {
                        if (!empty(comments)) {
                            licenseContent = comments;
                        } else {
                            err("processPom: no license found for " + name);
                            return artifacts;
                        }
                    }
                }
                artifacts.add(new Artifact()
                        .setType(ArtifactType.maven)
                        .setName(groupId+"."+artifactId)
                        .addLicense(new License()
                                .setName(name)
                                .setUrl(url)
                                .setText(licenseContent)));
            }
        }
        return artifacts;
    }

    private boolean emptyLicense(String content) {
        return empty(content) || content.startsWith("302 Found\n\n");
    }

    private synchronized List<Artifact> processPackageJson(File f) {

        final List<Artifact> artifacts = new ArrayList<>();
        final String[] parts = abs(f).split("/");
        if (parts.length < 2) {
            err("processPackageJson("+abs(f)+"): bad filename: "+abs(f));
            return artifacts;
        }
        final String name = parts[parts.length - 2];

        final JsonNode node = fromJsonOrDie(f, JsonNode.class);
        String spdxLicense = jsonText(node, "license");
        String licenseText = jsonText(node, "licenseText");
        String url = null;
        String licenseContent = fileLicense(f.getParentFile());
        if (licenseContent == null) {
            if (!empty(licenseText)) {
                licenseContent = licenseText;
            } else {
                licenseContent = spdxLicenseContent(normalizeSpdxLicense(spdxLicense));
                if (licenseContent == null) {
                    err("processPackageJson(" + abs(f) + "): spdxLicense text could not be loaded");
                    return artifacts;
                }
                url = spdxLicenseUrl(normalizeSpdxLicense(spdxLicense));
            }
        }

        artifacts.add(new Artifact()
                .setName(name)
                .setType(ArtifactType.npm)
                .addLicense(new License()
                        .setName(spdxLicense)
                        .setUrl(url)
                        .setText(licenseContent)));
        return artifacts;
    }

    public static final String[] LICENSE_PREFIXES = {"LICENSE", "LICENCE"};

    private String fileLicense(File dir) {
        for (String prefix : LICENSE_PREFIXES) {
            try {
                final File[] licenseFiles = dir.listFiles(pathname -> pathname.getName().toLowerCase().startsWith(prefix.toLowerCase()) && !pathname.getName().toLowerCase().equals(prefix.toLowerCase()+".docs"));
                if (licenseFiles == null || licenseFiles.length == 0) continue;
                if (licenseFiles.length > 1) {
                    err("fileLicense(" + abs(dir) + ": multiple license files found: " + Arrays.toString(licenseFiles));
                    return null;
                }
                return FileUtil.toStringOrDie(licenseFiles[0]);
            } catch (Exception e) {
                return null;
            }
        }
        err("fileLicense(" + abs(dir) + ": no license files found in: " + abs(dir));
        return null;
    }

    private String normalizeSpdxLicense(String spdxLicense) {
        if (spdxLicense == null) return null;
        if (spdxLicense.contains(" OR ")) {
            spdxLicense = spdxLicense.replace("(", "").replace(")", "").split("\\s+")[0];
        }
        switch (spdxLicense) {
            case "BSD": return "BSD-2-Clause";
            case "CDDLv1.0": return "CDDL-1.0";
            case "CDDL+GPL": return "CDDL-1.1";
            case "Apache License, Version 2.0": return "Apache-2.0";
            default: return spdxLicense;
        }
    }

    private String jsonText(JsonNode node, String property) {
        final JsonNode propNode = node.get(property);
        return propNode == null ? null : propNode.textValue();
    }

    private String spdxLicenseContent(String spdxLicense) {
        try {
            return url2stringFollowRedirects(spdxLicenseUrl(spdxLicense));
        } catch (Exception e) {
            return null;
        }
    }

    private String url2stringFollowRedirects(String url) {
        try {
            return execScript("wget '" + url + "' -O - 2> /dev/null");
        } catch (Exception e) {
            return null;
        }
    }

    private String spdxLicenseUrl(String spdxLicense) {
        return "https://raw.githubusercontent.com/spdx/license-list/master/"+spdxLicense+".txt";
    }

}
