package allrightsy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Accessors;
import org.cobbzilla.util.collection.ArrayUtil;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.daemon.ZillaRuntime.hashOf;

@NoArgsConstructor @Accessors(chain=true)
@EqualsAndHashCode(of={"name", "type"}) @ToString(of={"name", "type"})
public class Artifact implements Comparable<Artifact> {

    @Getter @Setter private String name;
    @Getter @Setter private ArtifactType type;

    @Setter private String artifactTypeName;
    public String getArtifactTypeName () {
        return !empty(artifactTypeName) ? artifactTypeName : type == null ? null : type.getArtifactName();
    }

    @Getter @Setter private License[] licenses;

    @JsonIgnore @Getter(lazy=true) private final String id = hashOf(getName(), getType(), getLicenses());

    public Artifact addLicense(License license) {
        licenses = ArrayUtil.append(licenses, license);
        return this;
    }

    @Override public int compareTo(Artifact o) {
        int diff = o.getName().compareTo(getName());
        if (diff != 0) return diff;
        return o.getType().compareTo(getType());
    }

}
