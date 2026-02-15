package enterprises.iwakura.docs.object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * Defines a documentation entry point. This is shown in the documentation list view as the header for all subsequent
 * topics.
 */
@Data
@Builder
@AllArgsConstructor
public class Documentation {

    // TODO: Image

    /**
     * Group supplying the documentation. E.g. Mod's group or the server's name.
     */
    private final String group;
    /**
     * Unique ID for the documentation. Should be without spaces.
     */
    private final String id;
    /**
     * Human-readable name for the documentation. It is shown in the UI.
     */
    private final String name;

    /**
     * The documentation type.
     */
    private final DocumentationType type;

    /**
     * List of topics supplied by the documentation.
     */
    private final List<Topic> topics = new ArrayList<>();

    /**
     * User defined sorting index
     */
    private Integer sortIndex;

    /**
     * Adds topics to this documentation.
     *
     * @param topics Topics
     *
     * @return Current instance
     */
    public Documentation addTopics(@NonNull Topic... topics) {
        this.topics.addAll(Arrays.asList(topics));
        return this;
    }

    /**
     * Adds topics to this documentation.
     *
     * @param topics Topics
     *
     * @return Current instance
     */
    public Documentation addTopics(@NonNull List<Topic> topics) {
        this.topics.addAll(topics);
        return this;
    }

    /**
     * Recursively finds topic by its ID
     *
     * @param topicId Topic ID
     *
     * @return Optional of Topic
     */
    public Optional<Topic> findTopicById(String topicId) {
        for (Topic topic : topics) {
            var foundTopic = topic.findTopicById(topicId);
            if (foundTopic.isPresent()) {
                return foundTopic;
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
    public String toString() {
        return "Documentation{" +
            "group='" + group + '\'' +
            ", id='" + id + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Documentation that)) {
            return false;
        }
        return Objects.equals(group, that.group) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, id);
    }
}
