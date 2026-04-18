package enterprises.iwakura.docs.config;

import java.util.ArrayList;
import java.util.List;

import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.LocaleType;
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
    private LocaleType localeType;
    private int sortIndex = 0;
    private boolean category;
    private String markdownContent;
    private List<String> subTopics;
    private List<String> requiredPermissions;

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

        if (markdownContent == null) {
            markdownContent = "";
        }

        if (localeType == null) {
            localeType = LocaleType.ENGLISH;
        }

        return new Topic(
            id,
            name,
            description,
            author,
            localeType,
            sortIndex,
            category,
            markdownContent,
            documentation,
            requiredPermissions
        );
    }

    /**
     * Creates unique ID specifying just this topic. Uses ID and its locale.
     *
     * @return Unique ID
     */
    public String createUniqueId() {
        if (localeType == null) {
            return id;
        } else {
            return id + "$" + localeType.getCode();
        }
    }
}
