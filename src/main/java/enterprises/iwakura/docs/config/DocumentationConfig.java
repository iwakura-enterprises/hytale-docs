package enterprises.iwakura.docs.config;

import java.util.List;

import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.DocumentationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DocumentationConfig {

    private String group;
    private String id;
    private String name;
    private boolean enabled = true;
    private Integer sortIndex;

    /**
     * Creates server documentation out of the current instance
     *
     * @return Documentation
     */
    public Documentation toDocumentation(DocumentationType type) {
        if (group == null || group.isBlank()) {
            throw new IllegalArgumentException("Documentation config cannot have empty group!");
        }

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Documentation config cannot have empty id!");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Documentation config cannot have empty name!");
        }

        return new Documentation(group, id, name, type, sortIndex);
    }

    @Override
    public String toString() {
        return "DocumentationConfig{" +
            "group='" + group + '\'' +
            ", id='" + id + '\'' +
            '}';
    }
}
