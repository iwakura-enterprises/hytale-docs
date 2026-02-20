package enterprises.iwakura.docs.object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

/**
 * Defines a topic inside {@link Documentation}. Can have sub-topics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Topic {

    // TODO: Image

    /**
     * List of sub-topics
     */
    private final List<Topic> topics = new ArrayList<>();
    /**
     * List of warnings
     */
    private final List<String> warnings = new ArrayList<>(); // TODO: Implement in renderer
    /**
     * Unique ID for the topic within the {@link Documentation}
     */
    private String id;
    /**
     * Human-readable name for the topic.
     */
    private String name;
    /**
     * The description for the topic.
     */
    private String description;
    /**
     * The author for the topic.
     */
    private String author;
    /**
     * The sort index for ordering topics within their branch.
     */
    private int sortIndex;
    /**
     * The Markdown parsable content for the topic.
     */
    private String markdownContent;
    /**
     * The documentation that the topic belongs to.
     */
    @ToString.Exclude
    private Documentation documentation;

    /**
     * Checks whenever this topic is empty.
     *
     * @return True if yes, false otherwise
     */
    public boolean isEmpty() {
        return markdownContent == null || markdownContent.isEmpty();
    }

    /**
     * Checks whenever this topic has child topics
     *
     * @return True if yes, false otherwise
     */
    public boolean hasTopics() {
        return !topics.isEmpty();
    }

    /**
     * Adds topics
     *
     * @param topics Topics
     *
     * @return Current instance
     */
    public Topic addTopics(@NonNull Topic... topics) {
        this.topics.addAll(Arrays.asList(topics));
        return this;
    }

    /**
     * Adds warning
     *
     * @param message Message
     */
    public void addWarning(String message) {
        warnings.add(message);
    }

    /**
     * Recursively finds topic by its ID
     *
     * @param topicId Topic ID
     *
     * @return Optional of Topic
     */
    public Optional<Topic> findTopicById(String topicId) {
        if (topicId.equals(id)) {
            return Optional.of(this);
        } else {
            for (Topic topic : topics) {
                var foundTopic = topic.findTopicById(topicId);
                if (foundTopic.isPresent()) {
                    return foundTopic;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Counts recursively number of topics
     *
     * @return Number
     */
    public int countTopics() {
        int count = topics.size();
        for (Topic topic : topics) {
            count += topic.countTopics();
        }
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Topic topic)) {
            return false;
        }
        return Objects.equals(id, topic.id) && Objects.equals(name, topic.name)
            && Objects.equals(documentation, topic.documentation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, documentation);
    }

    @Override
    public String toString() {
        return "Topic{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            '}';
    }

    /**
     * Returns the topic identifier
     *
     * @return Topic identifier
     */
    public String getTopicIdentifier() {
        if (documentation == null) {
            return id;
        }
        return "%s:%s:%s".formatted(
            documentation.getGroup(), documentation.getId(), id
        );
    }
}
