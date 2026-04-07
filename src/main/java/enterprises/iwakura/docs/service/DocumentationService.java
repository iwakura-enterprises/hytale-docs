package enterprises.iwakura.docs.service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.gson.Gson;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

import enterprises.iwakura.docs.VoileAPI;
import enterprises.iwakura.docs.DocsPlugin;
import enterprises.iwakura.docs.config.DocumentationIndexConfig;
import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.DocumentationType;
import enterprises.iwakura.docs.object.LoaderContext;
import enterprises.iwakura.docs.object.LocaleType;
import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.docs.service.loader.DocumentationLoader;
import enterprises.iwakura.docs.service.loader.UniversalDocumentationLoader;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class DocumentationService {

    private final ConfigurationService configurationService;
    private final MarkdownService markdownService;
    private final Gson gson;
    private final Logger logger;
    private final DocsPlugin plugin;

    /**
     * Documentation loaders that are used when (re)loading documentations
     */
    private final Map<JavaPlugin, List<DocumentationLoader>> documentationLoaders = new HashMap<>();

    /**
     * Loaded documentations, from the file system or registered by other mods.
     */
    private final List<Documentation> loadedDocumentations = new ArrayList<>();
    private final Map<LocaleType, Integer> numberOfTranslatedTopicsForLocaleType = new HashMap<>();

    /**
     * Registers default documentation loaders
     */
    public void registerDocumentationLoaders() {
        logger.info("Registering default documentation loaders...");

        var serverDocumentationDirectory = plugin.getDataDirectory()
            .resolve(configurationService.getDocsConfig().getLoadDocumentationsFromDirectory());
        var serverDocumentationIndexFile = serverDocumentationDirectory
            .resolve("index.json");

        if (!Files.exists(serverDocumentationDirectory)) {
            logger.warn("Server documentation directory at " + serverDocumentationDirectory + " does not exist! Creating...");
            try {
                Files.createDirectories(serverDocumentationDirectory);
            } catch (IOException exception) {
                throw new RuntimeException("Failed to create server documentation directory at " + serverDocumentationDirectory, exception);
            }
        }

        if (!Files.exists(serverDocumentationIndexFile)) {
            logger.warn("Server documentation index file at " + serverDocumentationIndexFile + " does not exist! Creating...");
            try {
                Files.writeString(serverDocumentationIndexFile, gson.toJson(new DocumentationIndexConfig()));
            } catch (IOException exception) {
                throw new RuntimeException("Failed to create documentation index file at " + serverDocumentationIndexFile, exception);
            }
        }

        VoileAPI.get().register(plugin, new UniversalDocumentationLoader(
            DocumentationType.SERVER,
            serverDocumentationIndexFile
        ));

        try {
            // Won't be closed so we can access the file system when (re)loading the documentation.
            var voileFileSystem = FileSystems.newFileSystem(plugin.getFile());
            VoileAPI.get().register(plugin, new UniversalDocumentationLoader(
                DocumentationType.INTERNAL,
                voileFileSystem.getPath("internal-docs/index.json")
            ));
        } catch (Exception exception) {
            logger.error("Failed to open Voile's plugin file as a file system to load internal documentation", exception);
        }
    }

    /**
     * Registers documentation loader
     *
     * @param documentationLoader documentation loader
     */
    public void registerDocumentationLoader(JavaPlugin javaPlugin, DocumentationLoader documentationLoader) {
        documentationLoaders.compute(javaPlugin, (k, value) -> {
            var loaders = value != null ? value : new ArrayList<DocumentationLoader>();
            loaders.add(documentationLoader);
            return loaders;
        });
    }

    /**
     * Clears all loaded documentations and loads them again with registered documentation loaders
     */
    public void reloadDocumentations() {
        logger.info("Reloading documentations from %d loaders".formatted(documentationLoaders.size()));
        loadedDocumentations.clear();

        var loaderContext = new LoaderContext(logger, gson, markdownService, configurationService);

        for (var entry : documentationLoaders.entrySet()) {
            var plugin = entry.getKey();
            var loaders = entry.getValue();
            logger.info("# Plugin %s specified %d documentation loader(s), loading...".formatted(
                plugin.getName(), loaders.size()
            ));
            for (DocumentationLoader documentationLoader : loaders) {
                try {
                    logger.info("Loading %s @ %s".formatted(
                        plugin.getName(), documentationLoader.toString()
                    ));
                    loadedDocumentations.addAll(documentationLoader.load(loaderContext));
                } catch (Exception exception) {
                    logger.error("Failed to load documentations registered by plugin %s with loader %s (%s)".formatted(
                        plugin.getName(), documentationLoader, documentationLoader.getClass()
                    ), exception);
                }
            }
        }

        logger.info("Loaded %d documentations".formatted(loadedDocumentations.size()));
        logger.info("Calculating the number of translated topics for all locale types...");
        numberOfTranslatedTopicsForLocaleType.clear();
        loadedDocumentations.forEach(documentation -> documentation.getTopics().forEach(this::sumLocalizedTopics));
    }

    /**
     * Sums the number of localized topics for specified topic and its subtopics
     *
     * @param topic Topic
     */
    private void sumLocalizedTopics(Topic topic) {
        numberOfTranslatedTopicsForLocaleType.compute(
            topic.getLocaleType(),
            (localeType, count) -> count == null ? 1 : count + 1
        );
        if (!topic.getLocalizedTopics().isEmpty()) {
            topic.getLocalizedTopics().forEach(localizedTopic -> {
                numberOfTranslatedTopicsForLocaleType.compute(
                    localizedTopic.getLocaleType(),
                    (localeType, count) -> count == null ? 1 : count + 1
                );
            });
        }
        topic.getTopics().forEach(this::sumLocalizedTopics);
    }

    /**
     * Returns the number of localized topics for the specified locale type
     *
     * @param localeType Locale type
     *
     * @return Number of localized topics
     */
    public int getNumberOfLocalizedTopics(LocaleType localeType) {
        return numberOfTranslatedTopicsForLocaleType.computeIfAbsent(localeType, k -> 0);
    }

    /**
     * Returns unmodifiable list of enabled documentations filtered by the specified types
     *
     * @return Unmodifiable list of documentations
     */
    public List<Documentation> getEnabledDocumentations() {
        var docsConfig = configurationService.getDocsConfig();
        return getDocumentations(DocumentationType.ALL.stream()
            .filter(type -> !docsConfig.getDisabledDocumentationTypes().contains(type))
            .toList()
        );
    }

    /**
     * Returns unmodifiable list of documentations filtered by the specified types
     *
     * @param documentationTypes Documentation types
     *
     * @return Unmodifiable list of documentations
     */
    public List<Documentation> getDocumentations(List<DocumentationType> documentationTypes) {
        return loadedDocumentations.stream()
            .filter(documentation -> documentationTypes.contains(documentation.getType()))
            .sorted(Comparator.comparing((Documentation doc) -> doc.getType().ordinal())
                .thenComparing(Documentation::getSortIndex, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Documentation::getName))
            .toList();
    }

    /**
     * Returns default topic specified in documentation index. If none is specified, the first topic is returned.
     *
     * @param documentations Documentations to find the default configured topic
     * @param preferredLocaleType Preferred locale type to use when finding default topic
     *
     * @return Optional topic (empty if no topics were loaded)
     */
    public Optional<Topic> getDefaultTopic(List<Documentation> documentations, LocaleType preferredLocaleType) {
        if (documentations.isEmpty()) {
            return Optional.empty();
        }

        var docsConfig = configurationService.getDocsConfig();
        if (docsConfig.getDefaultTopicIdentifier() != null) {
            var defaultTopic = findTopic(documentations, docsConfig.getDefaultTopicIdentifier(), null, preferredLocaleType);
            if (defaultTopic.isPresent()) {
                return defaultTopic;
            }
        }

        // First found topic
        for (Documentation documentation : documentations) {
            Optional<Topic> firstTopic = documentation.getFirstTopic();
            if (firstTopic.isPresent()) {
                return firstTopic;
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a topic based on a colon-separated identifier string.
     * <p>
     * The topic string supports the following formats:
     * <ul>
     *   <li>{@code topicId} - searches for topic by ID only</li>
     *   <li>{@code groupOrId:topicId} - searches for topic within a documentation matching group or ID</li>
     *   <li>{@code group:documentationId:topicId} - searches for topic within a specific documentation group and
     *   ID</li>
     * </ul>
     *
     * @param documentations         Documentations to search in
     * @param topicIdentifier        the topic identifier string, using colons as delimiters
     * @param preferredDocumentation The preferred documentation to look first if topic identifier does not specify
     *                               documentation group or id
     * @param preferredLocaleType The preferred locale type to use, falls back to any other locale based on {@link LocaleType#ordinal()}
     *
     * @return an {@link Optional} containing the found {@link Topic}, or empty if not found
     *
     * @see #findTopic(List, String, String, String, LocaleType, boolean)
     */
    public Optional<Topic> findTopic(
        List<Documentation> documentations,
        String topicIdentifier,
        Documentation preferredDocumentation,
        LocaleType preferredLocaleType
    ) {
        boolean documentationGroupOrId = false;
        String documentationGroup = null;
        String documentationId = null;
        String topicId = null;
        LocaleType localeType = preferredLocaleType;
        String[] openTopicData = topicIdentifier.split(":");

        if (openTopicData.length == 1) {
            topicId = openTopicData[0];
            if (preferredDocumentation != null) {
                documentationGroup = preferredDocumentation.getGroup();
                documentationId = preferredDocumentation.getId();
            }
        } else if (openTopicData.length == 2) {
            documentationGroup = openTopicData[0];
            documentationId = openTopicData[0];
            topicId = openTopicData[1];
            documentationGroupOrId = true;
        } else if (openTopicData.length >= 3) {
            if (openTopicData.length > 3) {
                logger.warn("Incorrect topic identifier! Expected maximum of two colons: " + topicIdentifier);
            }
            documentationGroup = openTopicData[0];
            documentationId = openTopicData[1];
            topicId = openTopicData[2];
        }

        if (topicId == null) {
            return Optional.empty();
        }

        if (topicId.contains("$")) {
            var topicIdWithLocale = topicId.split("\\$");
            topicId = topicIdWithLocale[0];
            // Always override preferred locale type if specifically specified
            localeType = LocaleType.byCode(topicIdWithLocale[1]);
        }

        var optionalTopic = findTopic(documentations, documentationGroup, documentationId, topicId, localeType, documentationGroupOrId);

        if (optionalTopic.isEmpty() && openTopicData.length == 1) {
            // try searching without the preferred documentation
            optionalTopic = findTopic(documentations, null, null, topicId, localeType, false);
        }

        return optionalTopic;
    }

    /**
     * Finds topic specified by the parameters
     *
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
        List<Documentation> documentations, String documentationGroup,
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
            .map(topic -> topic.getLocalePreferredTopic(preferredLocaleType))
            .findFirst();
    }
}
