package enterprises.iwakura.docs.config;

import java.util.List;

import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.Topic;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TopicConfig {

    private String id;
    private String name;
    private String description;
    private String author;
    private int sortIndex = 0;
    private String markdownContent;
    private List<String> subTopics;

    public void setSubTopics(List<String> subTopics) {
        if (subTopics == null) {
            subTopics = List.of();
        }
        this.subTopics = subTopics;
    }

    /**
     * Converts current topic config to Topic for Documentation. Validates
     * required files (id, name, description, author)
     *
     * @param documentation Documentation
     *
     * @return Topic
     */
    public Topic toTopic(Documentation documentation) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Topic config cannot have empty id!");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Topic config cannot have empty name!");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Topic config cannot have empty description!");
        }

        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("Topic config cannot have empty author!");
        }

        return new Topic(id, name, description, author, sortIndex, markdownContent, documentation, null);
    }
}
