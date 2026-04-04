package enterprises.iwakura.docs.object;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import enterprises.iwakura.docs.util.BoyerMooreSearch.SearchPattern;
import enterprises.iwakura.docs.util.LocaleUtils;
import jdk.jfr.Experimental;
import lombok.AccessLevel;
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
     * Normalized Markdown content for full-text search
     */
    private transient String normalizedMarkdownContent;
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
        this.normalizedMarkdownContent = null;
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
     * Checks if this topic's name/content or its sub topics' name/content is contained in the search query
     *
     * @param topicSearchQuery Topic search query
     * @param fullTextSearch   If search should be done on the topic's content
     * @param preferredLocaleType Preferred topic locale type to search in
     *
     * @return True if yes, false otherwise
     */
    public boolean searchTopic(String topicSearchQuery, LocaleType preferredLocaleType, boolean fullTextSearch) {
        String normalizedQuery = LocaleUtils.normalize(topicSearchQuery);
        SearchPattern searchPattern = SearchPattern.of(normalizedQuery);
        return searchTopic(searchPattern, preferredLocaleType, fullTextSearch);
    }

    /**
     * Checks if this topic's name/content or its sub topics' name/content is contained in the search query
     *
     * @param searchPattern  Pre-built {@link SearchPattern}
     * @param fullTextSearch If search should be done on the topic's content
     * @param preferredLocaleType Preferred topic locale type to search in
     *
     * @return True if yes, false otherwise
     */
    public boolean searchTopic(SearchPattern searchPattern, LocaleType preferredLocaleType, boolean fullTextSearch) {
        boolean matchesTopicContent = fullTextSearch && matchesTopicContentSearch(searchPattern, preferredLocaleType);

        if (matchesTopicContent || searchPattern.containedIn(LocaleUtils.normalize(this.getLocalePreferredTopic(preferredLocaleType).name))) {
            return true;
        } else {
            return topics.stream().anyMatch(topic -> topic.searchTopic(searchPattern, preferredLocaleType, fullTextSearch));
        }
    }

    /**
     * Checks whenever the search topic query is contained in ticket's content
     *
     * @param searchPattern Pre-computed search pattern
     * @param preferredLocaleType Preferred topic locale type to search in
     *
     * @return True if yes, false otherwise
     */
    private boolean matchesTopicContentSearch(SearchPattern searchPattern, LocaleType preferredLocaleType) {
        if (category) {
            return false; // Category has no content
        }

        var searchInTopic = this.getLocalePreferredTopic(preferredLocaleType);

        if (searchInTopic.normalizedMarkdownContent == null) {
            searchInTopic.normalizedMarkdownContent = LocaleUtils.normalize(searchInTopic.markdownContent);
        }

        return searchPattern.containedIn(searchInTopic.normalizedMarkdownContent);
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

    /**
     * returns first found non-category topic
     *
     * @return Optional of topic (empty if not found)
     */
    public Optional<Topic> getFirstTopic() {
        for (Topic topic : topics) {
            if (!topic.isCategory()) {
                return Optional.of(topic);
            } else {
                var subTopic = topic.getFirstTopic();
                if (subTopic.isPresent()) {
                    return subTopic;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Gets the locale preferred topic. Returns itself, if the locale type matches, otherwise finds the matching locale
     * topic from {@link #localizedTopics}. If no topic with the specified topic is found, uses the lowest
     * {@link LocaleType#ordinal()} that is found in {@link #localizedTopics} and the current topic instance.
     *
     * @param localeType Localized type
     *
     * @return Non-null topic (itself or one from {@link #localizedTopics}
     */
    public Topic getLocalePreferredTopic(LocaleType localeType) {
        int lowestLocalizedTopicLocaleTypeOrdinal = this.localeType.ordinal();
        Topic lowestLocalizedTopic = this;
        if (localeType == null || this.localeType == localeType) {
            return this;
        } else {
            for (Topic localizedTopic : localizedTopics) {
                if (localizedTopic.localeType == localeType) {
                    return localizedTopic;
                }
                if (localizedTopic.localeType.ordinal() < lowestLocalizedTopicLocaleTypeOrdinal) {
                    lowestLocalizedTopicLocaleTypeOrdinal = localizedTopic.localeType.ordinal();
                    lowestLocalizedTopic = localizedTopic;
                }
            }
        }
        return lowestLocalizedTopic;
    }
}
