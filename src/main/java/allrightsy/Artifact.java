package allrightsy;

import lombok.*;
import lombok.experimental.Accessors;
import org.cobbzilla.util.collection.ArrayUtil;

import java.util.ArrayList;
import java.util.List;

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
