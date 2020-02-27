package allrightsy;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ArtifactType {

    maven ("Maven artifact"), npm ("NPM package");

    @Getter private String artifactName;

}
