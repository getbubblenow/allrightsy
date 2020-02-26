package allrightsy;

import lombok.*;
import lombok.experimental.Accessors;

import static org.cobbzilla.util.reflect.ReflectionUtil.copy;

@NoArgsConstructor @Accessors(chain=true) @ToString(of={"name"})
public class License {

    @Getter @Setter private String name;
    @Getter @Setter private String url;
    @Getter @Setter private String text;

    public License(License license) { copy(this, license); }

}
