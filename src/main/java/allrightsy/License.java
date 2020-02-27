package allrightsy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.text.StringEscapeUtils;
import org.cobbzilla.util.string.StringUtil;
import org.cobbzilla.util.xml.TidyUtil;
import org.w3c.tidy.Tidy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.reflect.ReflectionUtil.copy;

@NoArgsConstructor @Accessors(chain=true) @ToString(of={"name"})
public class License {

    @Getter @Setter private String name;
    @Getter @Setter private String url;
    @Getter @Setter private String text;

    public License(License license) { copy(this, license); }

    @JsonIgnore public boolean isHtmlLicense () {
        return !empty(text) && StringUtil.truncate(text, 1000).toLowerCase().contains("<html");
    }

    @JsonIgnore public String getEscapedLicenseText () {

        if (empty(text)) return null;

        final Tidy tidy = TidyUtil.createTidy();
        final ByteArrayOutputStream out = new ByteArrayOutputStream(text.length());
        TidyUtil.parse(tidy, new ByteArrayInputStream(text.getBytes()), out, true);

        return StringEscapeUtils.escapeHtml4(out.toString());
    }

}
