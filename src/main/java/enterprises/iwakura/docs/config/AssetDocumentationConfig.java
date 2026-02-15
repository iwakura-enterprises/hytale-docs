package enterprises.iwakura.docs.config;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class AssetDocumentationConfig extends DocumentationConfig {

    private final List<Entry> topics = new ArrayList<>();

    @Data
    public static class Entry {

        private String file;
        private List<Entry> subTopics;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
