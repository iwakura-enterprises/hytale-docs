package enterprises.iwakura.docs.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ResourcesDocumentationIndexConfig {

    private DocumentationConfig documentation;
    private final List<Entry> topics = new ArrayList<>();

    @Data
    public static class Entry {

        private String file;
        private List<Entry> subTopics;
    }
}
