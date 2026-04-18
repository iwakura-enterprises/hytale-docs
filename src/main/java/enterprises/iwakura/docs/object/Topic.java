package enterprises.iwakura.docs.object;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import enterprises.iwakura.docs.service.DocumentationSearchService;
import enterprises.iwakura.docs.util.ListUtils;
import enterprises.iwakura.docs.util.LocaleUtils;
import jdk.jfr.Experimental;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Defines a topic inside {@link Documentation}. Can have sub-topics.
 */
@Data
public class Topic {

    // TODO: Image

    /**
     * List of sub-topics
     */
    private final List<Topic> topics = new ArrayList<>();
    /**
     * List of the current topic in different locales. The topics within this topic as if it was this current topic, e.g.
     * contains the same sub-topics array.
     */
    @ToString.Exclude
    private final List<Topic> localizedTopics = new ArrayList<>();
    /**
     * List of warnings
     */
    private final List<String> warnings = new ArrayList<>(); // TODO: Implement in renderer
    /**
     * List of required permissions
     */
    private final List<String> requiredPermissions = new ArrayList<>();
    /**
     * Unique ID for the topic within the {@link Documentation}
     */
    private @NonNull String id;
    /**
     * Human-readable name for the topic.
     */
    private @NonNull String name;
    /**
     * The description for the topic.
     */
    private @NonNull String description;
    /**
     * The author for the topic.
     */
    private @NonNull String author;
    /**
     * The locale for the topic.
     */
    private @NonNull LocaleType localeType;
    /**
     * The sort index for ordering topics within their branch.
     */
    private int sortIndex;
    /**
     * Determines if the topic should be treated as a category (non-clickable in the UI and does not contain any content)
     */
    private boolean category;
    /**
     * The Markdown parsable content for the topic.
     */
    private @NonNull String markdownContent;
    /**
     * Additional topic data
     */
    private final AdditionalTopicData additionalTopicData;
    /**
     * The documentation that the topic belongs to. Nullable.
     */
    @ToString.Exclude
    private Documentation documentation;

    /**
     * Non-null if topic was loaded from a file system.
     */
    private Path topicFilePath;

    /**
     * Callback that is invoked when the topic is opened by the player.
     */
    @Experimental
    private Consumer<DocsContext> topicOpenedCallback;

    public Topic(
        @NonNull String id,
        @NonNull String name,
        @NonNull String description,
        @NonNull String author,
        @NonNull LocaleType localeType,
        int sortIndex,
        boolean category,
        @NonNull String markdownContent,
        Documentation documentation,
        List<String> requiredPermissions
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.author = author;
        this.localeType = localeType;
        this.sortIndex = sortIndex;
        this.category = category;
        this.markdownContent = markdownContent;
        this.documentation = documentation;
        this.requiredPermissions.addAll(ListUtils.emptyIfNull(requiredPermissions));

        this.additionalTopicData = new AdditionalTopicData(
            LocaleUtils.normalize(name),
            LocaleUtils.normalize(markdownContent),
            new TopicIdentifier(
                documentation != null ? documentation.getGroup() : null,
                documentation != null ? documentation.getId() : null,
                id,
                localeType
            )
        );
    }

    /**
     * Checks whenever this topic is empty.
     *
     * @return True if yes, false otherwise
     */
    public boolean isEmpty() {
        return markdownContent.isEmpty();
    }

    /**
     * Sets the markdown content. If null, sets an empty string.
     *
     * @param markdownContent Markdown content
     */
    public void setMarkdownContent(String markdownContent) {
        this.markdownContent = Objects.requireNonNullElse(markdownContent, "");
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
     * Adds topics
     *
     * @param topics Topics
     *
     * @return Current instance
     */
    public Topic addTopics(List<Topic> topics) {
        this.topics.addAll(topics);
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
        return Objects.equals(id, topic.id)
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

    /**
     * Invokes the {@link #topicOpenedCallback} if non-null
     *
     * @param context DocsContext
     */
    public void invokeOpenedCallback(DocsContext context) {
        if (topicOpenedCallback != null) {
            topicOpenedCallback.accept(context);
        }
    }

    @Data
    @RequiredArgsConstructor
    public static class AdditionalTopicData {

        private final String normalizedName;
        private final String normalizedMarkdownContent;
        private final TopicIdentifier topicIdentifier;
    }
}
