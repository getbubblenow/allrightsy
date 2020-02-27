package allrightsy;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cobbzilla.util.collection.ArrayUtil;

@NoArgsConstructor @Accessors(chain=true) @ToString(of={"name", "type"})
public class Artifact {

    @Getter @Setter private String name;
    @Getter @Setter private ArtifactType type;
    @Getter @Setter private License[] licenses;

    public Artifact addLicense(License license) {
        licenses = ArrayUtil.append(licenses, license);
        return this;
    }
}
