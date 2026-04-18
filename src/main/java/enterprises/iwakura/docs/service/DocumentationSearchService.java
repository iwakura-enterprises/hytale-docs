package enterprises.iwakura.docs.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.InterfaceMode;
import enterprises.iwakura.docs.object.LocaleType;
import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.docs.util.BoyerMooreSearch.SearchPattern;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

/**
 * Service managing searching thru {@link enterprises.iwakura.docs.object.Documentation} and
 * {@link enterprises.iwakura.docs.object.Topic}.
 */
@Bean
@RequiredArgsConstructor
public class DocumentationSearchService {

    public static final Pattern TOPIC_IDENTIFIER_PATTERN = Pattern.compile("^(?:([^:$]+):(?:([^:$]+):)?)?([^:$]+)(?:\\$([^:$]+))?$");

    private final Logger logger;
    private final ConfigurationService configurationService;

    /**
     * Finds a topic based on a colon-separated identifier string.
     * <p>
     * The topic string supports the following formats:
     * <ul>
     *   <li>{@code topicId} - searches for topic by ID only</li>
     *   <li>{@code groupOrId:topicId} - searches for topic within a documentation matching group or ID</li>
     *   <li>{@code group:documentationId:topicId} - searches for topic within a specific documentation group and
     *   ID</li>
     *   <li>{@code group:documentationId:topicId$locale} - the previous </li>
     * </ul>
     *
     * @param playerRef              Player that the topic will be shown to
     * @param documentations         Documentations to search in
     * @param topicIdentifier        the topic identifier string, using colons as delimiters
     * @param preferredDocumentation The preferred documentation to look first if topic identifier does not specify
     *                               documentation group or id
     * @param preferredLocaleType The preferred locale type to use, falls back to any other locale based on {@link LocaleType#ordinal()}
     *
     * @return an {@link Optional} containing the found {@link Topic}, or empty if not found
     *
     * @see #findTopic(PlayerRef, List, String, String, String, LocaleType, boolean)
     */
    public Optional<Topic> findTopic(
        PlayerRef playerRef,
        List<Documentation> documentations,
        String topicIdentifier,
        Documentation preferredDocumentation,
        LocaleType preferredLocaleType
    ) {
        if (topicIdentifier == null) {
            return Optional.empty();
        }

        var matcher = TOPIC_IDENTIFIER_PATTERN.matcher(topicIdentifier);

        if (!matcher.find()) {
            return Optional.empty();
        }

        String documentationGroup = Optional.ofNullable(matcher.group(1))
            .or(() -> Optional.ofNullable(preferredDocumentation).map(Documentation::getGroup))
            .orElse(null);
        String documentationId = Optional.ofNullable(matcher.group(2))
            .or(() -> Optional.ofNullable(preferredDocumentation).map(Documentation::getId))
            .orElse(null);
        String topicId = matcher.group(3); // Always non-null, because of matcher.find()
        LocaleType localeType = Optional.ofNullable(matcher.group(4)).map(LocaleType::byCode)
            .or(() -> Optional.ofNullable(preferredLocaleType))
            .orElse(LocaleType.ENGLISH);

        var optionalTopic = findTopic(
            playerRef,
            documentations,
            documentationGroup,
            documentationId,
            topicId,
            localeType,
            Objects.equals(documentationGroup, documentationId)
        );

        if (optionalTopic.isEmpty()) {
            // try searching without the preferred documentation
            optionalTopic = findTopic(
                playerRef,
                documentations,
                null,
                null,
                topicId,
                localeType,
                Objects.equals(documentationGroup, documentationId)
            );
        }

        return optionalTopic;
    }

    /**
     * Finds topic specified by the parameters
     *
     * @param playerRef              Player that the topic will be shown to
     * @param documentations         Documentations to search in
     * @param documentationGroup     Optional documentation group
     * @param documentationId        Optional documentation ID
     * @param topicId                Topic ID
     * @param documentationGroupOrId If search should be done loosely on documentation group / id
     * @param preferredLocaleType    Preferred locale type
     *
     * @return Optional topic
     */
    public Optional<Topic> findTopic(
        PlayerRef playerRef,
        List<Documentation> documentations,
        String documentationGroup,
        String documentationId,
        String topicId,
        LocaleType preferredLocaleType,
        boolean documentationGroupOrId
    ) {
        if (topicId == null) {
            logger.warn("topicId cannot be null when invoking #findTopic()!");
            return Optional.empty();
        }
        return documentations.stream()
            .filter(documentation -> {
                if (documentationGroupOrId) {
                    return (documentationGroup == null || documentation.getGroup().equals(documentationGroup)
                        || documentationGroup.equals(documentationId))
                        && (documentationId == null || documentation.getId().equals(documentationId)
                        || documentation.getGroup().equals(documentationGroup));
                } else {
                    return (documentationGroup == null || documentation.getGroup().equals(documentationGroup))
                        && (documentationId == null || documentation.getId().equals(documentationId));
                }
            })
            .map(documentation -> documentation.findTopicById(topicId).orElse(null))
            .filter(Objects::nonNull)
            .filter(topic -> canSeeTopic(playerRef, topic))
            .map(topic -> getLocalePreferredTopic(topic, preferredLocaleType))
            .findFirst();
    }

    /**
     * Finds the default topic based on configuration, fallbacks to first visible topic for the player.
     * @param playerRef Player reference
     * @param documentations Documentations
     * @param preferredLocaleType Preferred locale type
     * @return Optional of Topic
     */
    public Optional<Topic> findDefaultTopic(
        PlayerRef playerRef,
        List<Documentation> documentations,
        LocaleType preferredLocaleType
    ) {
        if (documentations.isEmpty()) {
            return Optional.empty();
        }

        var docsConfig = configurationService.getDocsConfig();
        if (docsConfig.getDefaultTopicIdentifier() != null) {
            var defaultTopic = findTopic(playerRef, documentations, docsConfig.getDefaultTopicIdentifier(), null, preferredLocaleType);
            if (defaultTopic.isPresent()) {
                return defaultTopic;
            }
        }

        // First found topic
        for (Documentation documentation : documentations) {
            if (canSeeDocumentation(playerRef, documentation)) {
                Optional<Topic> firstTopic = findFirstTopic(playerRef, documentation.getTopics());
                if (firstTopic.isPresent()) {
                    return firstTopic;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds first visible non-category topic in list of topics for the player
     * @param playerRef Player reference
     * @param topics Topics
     * @return Optional of topic
     */
    private Optional<Topic> findFirstTopic(PlayerRef playerRef, List<Topic> topics) {
        for (Topic topic : topics) {
            if (canSeeTopic(playerRef, topic)) {
                if (!topic.isCategory()) {
                    return Optional.of(topic);
                } else {
                    var optionalSubTopic = findFirstTopic(playerRef, topic.getTopics());
                    if (optionalSubTopic.isPresent()) {
                        return optionalSubTopic;
                    }
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Determines whenever the player can see the specified topic
     *
     * @param playerRef Player reference
     * @param topic     Topic
     *
     * @return True if yes, false otherwise
     */
    private boolean canSeeTopic(PlayerRef playerRef, Topic topic) {
        var disabledTopics = configurationService.getDocsConfig().getParsedDisabledTopicIdentifiers();
        if (topic.getDocumentation() != null && !canSeeDocumentation(playerRef, topic.getDocumentation())) {
            return false;
        }
        if (disabledTopics.stream().anyMatch(topicIdentifier -> topicIdentifier.matches(topic.getAdditionalTopicData().getTopicIdentifier()))) {
            return false;
        }
        if (topic.getRequiredPermissions().stream().anyMatch(permission -> !PermissionsModule.get().hasPermission(playerRef.getUuid(), permission))) {
            return false;
        }
        return true;
    }

    /**
     * Determines whenever the player can see the specified documentation
     *
     * @param playerRef     Player ref
     * @param documentation Documentation
     *
     * @return True if yes, false otherwise
     */
    public boolean canSeeDocumentation(PlayerRef playerRef, Documentation documentation) {
        var disabledDocumentations = configurationService.getDocsConfig().getDisabledDocumentations();
        if (disabledDocumentations.contains(documentation.getGroup() + ":" + documentation.getId())
            || disabledDocumentations.contains(documentation.getGroup())
            || disabledDocumentations.contains(documentation.getId())
        ) {
            return false;
        }
        if (documentation.getRequiredPermissions().stream().anyMatch(permission -> !PermissionsModule.get().hasPermission(playerRef.getUuid(), permission))) {
            return false;
        }
        return true;
    }

    /**
     * Determines whenever the player can see any topic within the specified topics (and its sub-topics, recursively)
     *
     * @param playerRef Player reference
     * @param topics    Topics
     *
     * @return True if yes, false otherwise
     */
    public boolean canSeeAnyTopic(PlayerRef playerRef, List<Topic> topics) {
        return findFirstTopic(playerRef, topics).isPresent();
    }

    /**
     * Checks if documentation has any topic that contains the search query
     *
     * @param documentation       Documentation
     * @param searchPattern       Topic search pattern
     * @param fullTextSearch      If search should be done on the topic's content
     * @param preferredLocaleType Preferred topic locale type to search in
     *
     * @return True if yes, false otherwise
     */
    public boolean searchForTopic(
        Documentation documentation,
        SearchPattern searchPattern,
        LocaleType preferredLocaleType,
        boolean fullTextSearch
    ) {
        return documentation.getTopics().stream().anyMatch(topic -> searchTopic(topic, searchPattern, preferredLocaleType, fullTextSearch));
    }

    /**
     * Checks if this topic's name/content or its sub topics' name/content is contained in the search query
     *
     * @param topic               Topic
     * @param searchPattern       Pre-built {@link SearchPattern}
     * @param fullTextSearch      If search should be done on the topic's content
     * @param preferredLocaleType Preferred topic locale type to search in
     *
     * @return True if yes, false otherwise
     */
    public boolean searchTopic(
        Topic topic,
        SearchPattern searchPattern,
        LocaleType preferredLocaleType,
        boolean fullTextSearch
    ) {
        if (matchesTopicNameSearch(topic, searchPattern, preferredLocaleType)
                || (fullTextSearch && matchesTopicContentSearch(topic, searchPattern, preferredLocaleType))
        ) {
            return true;
        } else {
            return topic.getTopics().stream().anyMatch(subTopic -> searchTopic(subTopic, searchPattern, preferredLocaleType, fullTextSearch));
        }
    }

    /**
     * Checks whenever the search topic query is contained in topic's name
     *
     * @param topic               Topic
     * @param searchPattern       Pre-computed search pattern
     * @param preferredLocaleType Preferred topic locale type to search in
     *
     * @return True if yes, false otherwise
     */
    public boolean matchesTopicContentSearch(
        Topic topic,
        SearchPattern searchPattern,
        LocaleType preferredLocaleType
    ) {
        if (topic.isCategory()) {
            return false; // Category has no content
        }

        var searchInTopic = this.getLocalePreferredTopic(topic, preferredLocaleType);
        var additionalTopicData = searchInTopic.getAdditionalTopicData();
        return searchPattern.containedIn(additionalTopicData.getNormalizedMarkdownContent());
    }

    /**
     * Checks whenever the search topic query is contained in topic's name
     *
     * @param topic               Topic
     * @param searchPattern       search pattern
     * @param preferredLocaleType Preferred topic locale to search in
     *
     * @return True if yes, false otherwise
     */
    public boolean matchesTopicNameSearch(
        Topic topic,
        SearchPattern searchPattern,
        LocaleType preferredLocaleType
    ) {
        var searchInTopic = this.getLocalePreferredTopic(topic, preferredLocaleType);
        var additionalTopicData = searchInTopic.getAdditionalTopicData();
        return searchPattern.containedIn(additionalTopicData.getNormalizedName());
    }

    /**
     * Gets the locale preferred topic. Returns itself, if the locale type matches, otherwise finds the matching locale
     * topic from {@link Topic#getLocalizedTopics()}. If no topic with the specified topic is found, uses the lowest
     * {@link LocaleType#ordinal()} that is found in {@link Topic#getLocalizedTopics()} and the current topic instance.
     *
     * @param topic      Topic
     * @param localeType Localized type
     *
     * @return Non-null topic (itself or one from {@link Topic#getLocalizedTopics()}
     */
    public Topic getLocalePreferredTopic(
        Topic topic,
        LocaleType localeType
    ) {
        int lowestLocalizedTopicLocaleTypeOrdinal = topic.getLocaleType().ordinal();
        Topic lowestLocalizedTopic = topic;
        if (localeType == null || topic.getLocaleType() == localeType) {
            return topic;
        } else {
            for (Topic localizedTopic : topic.getLocalizedTopics()) {
                if (localizedTopic.getLocaleType() == localeType) {
                    return localizedTopic;
                }
                if (localizedTopic.getLocaleType().ordinal() < lowestLocalizedTopicLocaleTypeOrdinal) {
                    lowestLocalizedTopicLocaleTypeOrdinal = localizedTopic.getLocaleType().ordinal();
                    lowestLocalizedTopic = localizedTopic;
                }
            }
        }
        return lowestLocalizedTopic;
    }

    /**
     * Returns all available interface modes to the player
     *
     * @param playerRef      Player ref
     * @param documentations Documentations
     *
     * @return List of available interface modes
     */
    public List<InterfaceMode> getAvailableInterfaceModes(PlayerRef playerRef, List<Documentation> documentations) {
        return configurationService.getDocsConfig().getAvailableInterfaceModes().stream()
            .filter(interfaceMode -> documentations.stream().anyMatch(documentation -> interfaceMode.has(documentation.getType()) && canSeeDocumentation(playerRef, documentation)))
            .toList();
    }
}
