package allrightsy.main;

import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.util.main.BaseMainOptions;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AllRightsyOptions extends BaseMainOptions {

    public static final String USAGE_PACKS = "Comma-separated list of sources. Can be pom.xml files, node_modules dirs, or .json file containing an array of pre-defined Artifacts";
    public static final String OPT_PACKS = "-p";
    public static final String LONGOPT_PACKS = "--packs";
    @Option(name=OPT_PACKS, aliases=LONGOPT_PACKS, usage=USAGE_PACKS, required=true)
    @Getter @Setter private String packs;

    public static final String USAGE_OUTFILE = "Output file, should end in .json";
    public static final String OPT_OUTFILE = "-o";
    public static final String LONGOPT_OUTFILE = "--outfile";
    @Option(name=OPT_OUTFILE, aliases=LONGOPT_OUTFILE, usage=USAGE_OUTFILE, required=true)
    @Getter @Setter private File outFile;

    public List<File> getPackageFiles() {
        return Arrays.stream(packs.split(",")).map(File::new).collect(Collectors.toList());
    }

}
