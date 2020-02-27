package allrightsy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cobbzilla.util.collection.ArrayUtil;

import static org.cobbzilla.util.daemon.ZillaRuntime.hashOf;

@NoArgsConstructor @Accessors(chain=true) @ToString(of={"name", "type"})
public class Artifact {

    @Getter @Setter private String name;
    @Getter @Setter private ArtifactType type;
    @Getter @Setter private License[] licenses;

    @JsonIgnore @Getter(lazy=true) private final String id = hashOf(getName(), getType(), getLicenses());

    public Artifact addLicense(License license) {
        licenses = ArrayUtil.append(licenses, license);
        return this;
    }
}
