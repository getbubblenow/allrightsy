package allrightsy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cobbzilla.util.string.StringUtil;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.reflect.ReflectionUtil.copy;

@NoArgsConstructor @Accessors(chain=true) @ToString(of={"name"})
public class License {

    @Getter @Setter private String name;
    @Getter @Setter private String url;
    @Getter @Setter private String text;

    public License(License license) { copy(this, license); }

    @JsonIgnore public boolean isHtmlLicense () {
        return !empty(text) && StringUtil.truncate(text, 1000).toLowerCase().matches("<\\s*html");
    }

    @JsonIgnore public String getEscapedLicenseText () {
        return empty(text) ? null : text.replace("\\", "\\\\").replace("'", "\\'");
    }

}
