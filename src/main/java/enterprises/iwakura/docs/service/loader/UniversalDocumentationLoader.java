package enterprises.iwakura.docs.service.loader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import enterprises.iwakura.docs.config.DocumentationConfig;
import enterprises.iwakura.docs.config.DocumentationIndexConfig;
import enterprises.iwakura.docs.config.TopicConfig;
import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.DocumentationType;
import enterprises.iwakura.docs.object.LoaderContext;
import enterprises.iwakura.docs.object.LocaleType;
import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.docs.util.Logger;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * Loads any documentation from {@link Path}. If loading documentations for a jar files,
 * you should use {@link FileSystems#newFileSystem(Path)} to load them from the file system.
 * <br>
 * Documentations from JAR files will correctly use assets loaded from assets and won't register
 * relative images as runtime image assets.
 */
@RequiredArgsConstructor
@AllArgsConstructor
public class UniversalDocumentationLoader extends DocumentationLoader {

    protected final DocumentationType documentationType;
    protected final Path documentationIndexFile;
    protected ClassLoader pluginClassLoader;

    @Override
    public List<Documentation> load(LoaderContext loaderContext) {
        var logger = loaderContext.getLogger();
        var documentations = new ArrayList<Documentation>();

        logger.info("Loading documentation index file from " + documentationIndexFile);

        if (!Files.exists(documentationIndexFile)) {
            logger.warn("Documentation index file " + documentationIndexFile + " does not exist!");
            return List.of();
        }

        // Load documentation index config
        DocumentationIndexConfig indexConfig;
        try {
            indexConfig = loaderContext.getGson().fromJson(Files.readString(documentationIndexFile), DocumentationIndexConfig.class);
        } catch (Exception exception) {
            logger.error("└ Failed to load index.json: %s".formatted(documentationIndexFile), exception);
            return List.of();
        }
        logger.info("└ Documentation index %s defines %d documentations".formatted(
            documentationIndexFile, indexConfig.getDocumentations().size()
        ));

        // Go thru documentations defined in the config
        for (DocumentationConfig documentationConfig : indexConfig.getDocumentations()) {
            if (!documentationConfig.isEnabled()) {
                logger.warn("└ Skipping disabled " + documentationConfig);
                continue;
            }

            if (documentationType == DocumentationType.MOD && !documentationConfig.getCompatibility().getMod().isUniversalDocumentationLoader()) {
                logger.warn("└ Documentation index file for a mod located at " + documentationIndexFile + " does not support new UniversalDocumentationLoader.");
                logger.warn("  Please refer to Voile's documentation how to enable UniversalDocumentationLoader compatibility.");
                logger.warn("  Loading mod's documentation using old deprecated ResourceDocumentationLoader...");
                var resourceDocumentations = new ResourcesDocumentationLoader(
                    documentationType,
                    pluginClassLoader,
                    documentationIndexFile.toString()
                ).load(loaderContext);
                documentations.addAll(resourceDocumentations);
            } else {
                try {
                    // Load documentation and its topics
                    var documentation = loadDocumentation(loaderContext, documentationConfig);
                    documentations.add(documentation);
                    logger.info("└ Loaded %s with %d topics".formatted(
                        documentation, documentation.countTopics()
                    ));
                } catch (Exception exception) {
                    logger.error("└ Failed to load " + documentationConfig, exception);
                }
            }
        }
        logger.info("└ Loaded %d documentations from %s".formatted(documentations.size(), documentationIndexFile));

        return documentations;
    }

    /**
     * Loads specific documentation
     *
     * @param loaderContext       Loader context
     * @param documentationConfig Documentation config
     *
     * @return Loaded documentation
     *
     * @throws IOException If any error occurs
     */
    protected Documentation loadDocumentation(
        LoaderContext loaderContext,
        DocumentationConfig documentationConfig
    ) throws IOException {
        var documentation = documentationConfig.toDocumentation(documentationType);
        var logger = loaderContext.getLogger();
        Path resolvedDocumentationRootDirectory = null;
        var groupIdDocumentationRootDirectory = documentationIndexFile.getParent().resolve("%s_%s".formatted(
            documentationConfig.getGroup(), documentationConfig.getId()
        ));
        var idDocumentationRootDirectory = documentationIndexFile.getParent().resolve(documentationConfig.getId());

        // Resolve documentation root directory based on the group_id or just id directory
        if (Files.exists(groupIdDocumentationRootDirectory)) {
            resolvedDocumentationRootDirectory = groupIdDocumentationRootDirectory;
        } else if (Files.exists(idDocumentationRootDirectory)) {
            resolvedDocumentationRootDirectory = idDocumentationRootDirectory;
        }

        // Could not resolve documentation root directory
        if (resolvedDocumentationRootDirectory == null) {
            logger.warn("└ Could not resolve documentation root directory (not found/check spelling) for documentation config " + documentationConfig);
            logger.warn("   - Tried: " + groupIdDocumentationRootDirectory);
            logger.warn("   - Tried: " + idDocumentationRootDirectory);
            return documentation;
        }

        List<Path> markdownFiles;
        // Keyed by uniqueId (id + locale) to avoid collision between localized variants of the same topic
        var pathToTopicConfig = new HashMap<Path, TopicConfig>();
        var pathToTopic = new HashMap<Path, Topic>();
        var uniqueTopicMap = new HashMap<String, Topic>();
        // Keyed by base topic id; used for sub-topic resolution and circular reference checks.
        // Holds the primary (lowest locale ordinal) config for each base id.
        var configMap = new HashMap<String, TopicConfig>();
        // Keyed by base topic id; points to the primary topic's file path
        var topicIdToPath = new HashMap<String, Path>();
        // Tracks base topic ids that have been placed as children in the tree
        var childTopicIds = new HashSet<String>();

        if (!Files.exists(resolvedDocumentationRootDirectory)) {
            logger.warn("└ Directory %s does not exist, creating..." + resolvedDocumentationRootDirectory);
            Files.createDirectories(resolvedDocumentationRootDirectory);
        }

        try (var pathStream = Files.walk(resolvedDocumentationRootDirectory)) {
            markdownFiles = pathStream.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".md"))
                .toList();
        } catch (IOException exception) {
            throw new IOException("Failed to read documentation root directory %s for topics!".formatted(
                resolvedDocumentationRootDirectory
            ), exception);
        }

        logger.info("└ Found %d markdown files in %s".formatted(
            markdownFiles.size(), resolvedDocumentationRootDirectory
        ));

        for (var markdownFile : markdownFiles) {
            try {
                var topicConfig = loaderContext.getMarkdownService().readMarkdownAsTopicConfig(
                    markdownFile.getFileName().toString(),
                    Files.readString(markdownFile, StandardCharsets.UTF_8),
                    documentationType != DocumentationType.MOD
                        ? loaderContext.getConfigurationService().getDocsConfig().getInterfacePreferencesDefaults().getLocaleType()
                        : null
                );
                pathToTopicConfig.put(markdownFile, topicConfig);

                // configMap holds one (primary) config per base id for sub-topic / circular-reference resolution.
                // We keep the config with the lowest locale ordinal so that sub-topic lists are resolved
                // from the primary locale's perspective.
                var baseId = topicConfig.getId();
                var existing = configMap.get(baseId);

                // If we found existing topic with the same locale type, this means there's a same topic by ID and
                // that it already has this language. This can unintentionally occur when loading server's topics and
                // the server has set default locale type and the same locale type is used for non-Locale specified topic
                // and then specified topic (e.g. my-topic and my-topic$cs while having the default language as CZECH).
                // In that case it would make one of the topics invisible w/o any error.
                if (existing != null && topicConfig.getLocaleType() == existing.getLocaleType()) {
                    logger.warn(("Topic ID %s has two or more markdown files that specify the same locale! Locale type for markdown "
                        + "file %s will be set to CHAOS.").formatted(baseId, markdownFile));
                    topicConfig.setLocaleType(LocaleType.CHAOS);
                }

                if (existing == null || topicConfig.getLocaleType().ordinal() < existing.getLocaleType().ordinal()) {
                    configMap.put(baseId, topicConfig);
                    topicIdToPath.put(baseId, markdownFile);
                }
            } catch (Exception exception) {
                throw new IOException("Failed to load Markdown file %s as topic".formatted(markdownFile), exception);
            }
        }

        for (var entry : pathToTopicConfig.entrySet()) {
            var file = entry.getKey();
            var topicConfig = entry.getValue();

            try {
                var topic = topicConfig.toTopic(documentation);
                topic.setTopicFilePath(file); // Can load to file in different filesystems
                pathToTopic.put(file, topic);
                uniqueTopicMap.put(topicConfig.createUniqueId(), topic);
            } catch (Exception exception) {
                logger.error("└ Invalid topic config, skipping %s (%s)".formatted(
                    file, topicConfig
                ));
            }
        }

        // Group all topic instances by their base id, then determine the primary topic (lowest locale ordinal)
        // and attach the remaining localized variants to it via localizedTopics.
        var primaryTopicMap = new HashMap<String, Topic>();
        for (var topic : uniqueTopicMap.values()) {
            var baseId = topic.getId();
            var current = primaryTopicMap.get(baseId);
            if (current == null || topic.getLocaleType().ordinal() < current.getLocaleType().ordinal()) {
                if (current != null) {
                    // The previously stored topic is now a secondary locale — move it to the new primary's localizedTopics
                    topic.getLocalizedTopics().add(current);
                    topic.getLocalizedTopics().addAll(current.getLocalizedTopics());
                    current.getLocalizedTopics().clear();
                }
                primaryTopicMap.put(baseId, topic);
            } else {
                current.getLocalizedTopics().add(topic);
            }
        }

        // Build sub-topic trees using primary topics. Sub-topic IDs in configs are always base ids.
        for (var topicConfig : configMap.values()) {
            var topic = primaryTopicMap.get(topicConfig.getId());
            if (topic == null || topicConfig.getSubTopics() == null) {
                continue;
            }

            // Get the parent topic's file directory for relative path resolution
            var parentFilePath = topicIdToPath.get(topicConfig.getId());
            var parentDir = parentFilePath != null ? parentFilePath.getParent() : resolvedDocumentationRootDirectory;

            for (var subTopicId : topicConfig.getSubTopics()) {
                // Check if subTopicId is a directory
                var potentialDir = parentDir.resolve(subTopicId);
                if (Files.isDirectory(potentialDir)) {
                    // Load all primary topics from the directory and attach them as sub-topics
                    var dirTopics = loadTopicsFromDirectory(potentialDir, pathToTopic, primaryTopicMap, topicConfig.getId(), configMap, topic, logger);
                    for (var dirTopic : dirTopics) {
                        topic.getTopics().add(dirTopic);
                        childTopicIds.add(dirTopic.getId());
                    }
                    continue;
                }

                var subTopic = primaryTopicMap.get(subTopicId);
                if (subTopic == null) {
                    var warning = "Sub-topic not found: %s".formatted(subTopicId);
                    topic.getWarnings().add(warning);
                    logger.warn("└ " + warning);
                    continue;
                }

                // Check for circular reference
                var visitedPath = new ArrayList<String>();
                if (hasCircularReference(topicConfig.getId(), subTopicId, configMap, visitedPath)) {
                    var loopPath = String.join(" -> ", visitedPath) + " -> " + visitedPath.getFirst();
                    var warning = "Topic loop detected: %s, sub-topic '%s' not added to '%s'".formatted(
                        loopPath, subTopicId, topicConfig.getId()
                    );
                    topic.getWarnings().add(warning);
                    logger.warn("└ " + warning);
                    continue;
                }

                topic.getTopics().add(subTopic);
                childTopicIds.add(subTopicId);
            }

            // Sort subtopics by sortIndex within this branch
            topic.getTopics().sort(Comparator.comparingInt(Topic::getSortIndex));
        }

        // Remove topics that were pulled in via directory loading but are also nested deeper within
        // another sibling's sub-tree via explicit sub-topics. Without this pass such topics appear
        // twice: once as a direct child (directory) and once in their intended nested position.
        for (var primaryTopic : primaryTopicMap.values()) {
            if (primaryTopic.getTopics().size() <= 1) {
                continue;
            }
            var deepChildIds = new HashSet<String>();
            for (var child : primaryTopic.getTopics()) {
                collectDescendantIds(child, deepChildIds);
            }
            primaryTopic.getTopics().removeIf(child -> deepChildIds.contains(child.getId()));
        }

        // Propagate sub-topic trees to localized copies.
        for (var primaryTopic : primaryTopicMap.values()) {
            for (var localizedTopic : primaryTopic.getLocalizedTopics()) {
                localizedTopic.getTopics().addAll(primaryTopic.getTopics());
            }
        }

        var rootTopics = primaryTopicMap.values().stream()
            .filter(topic -> !childTopicIds.contains(topic.getId()))
            .sorted(Comparator.comparingInt(Topic::getSortIndex))
            .toList();
        documentation.addTopics(rootTopics);
        return documentation;
    }

    /**
     * Loads primary topics from a directory, treating direct .md files as sub-topics.
     * For each file, only the primary topic (lowest locale ordinal) is added to the result;
     * localized variants are already attached via {@link Topic#getLocalizedTopics()}.
     * Does not recurse into subdirectories.
     */
    protected List<Topic> loadTopicsFromDirectory(
        Path directory,
        Map<Path, Topic> pathToTopic,
        Map<String, Topic> primaryTopicMap,
        String parentId,
        Map<String, TopicConfig> configMap,
        Topic parentTopic,
        Logger logger
    ) {
        var topics = new ArrayList<Topic>();
        var seenPrimaryIds = new HashSet<String>();

        try (var stream = Files.list(directory)) {
            var entries = stream.toList();

            for (var entry : entries) {
                if (Files.isRegularFile(entry) && entry.toString().endsWith(".md")) {
                    // Resolve the topic instance for this file path
                    var fileTopic = pathToTopic.get(entry);
                    if (fileTopic == null) {
                        logger.warn("└ No topic found for file: %s".formatted(entry));
                        continue;
                    }

                    // Use the primary topic for this base id (not the locale-specific file topic)
                    var matchingTopic = primaryTopicMap.get(fileTopic.getId());
                    if (matchingTopic == null) {
                        logger.warn("└ No primary topic found for id: %s".formatted(fileTopic.getId()));
                        continue;
                    }

                    // Skip if the primary topic for this base id was already added by a sibling locale file
                    if (!seenPrimaryIds.add(matchingTopic.getId())) {
                        continue;
                    }

                    // Check for circular reference
                    var visitedPath = new ArrayList<String>();
                    if (hasCircularReference(parentId, matchingTopic.getId(), configMap, visitedPath)) {
                        var loopPath = String.join(" -> ", visitedPath) + " -> " + visitedPath.getFirst();
                        var warning = "Topic loop detected: %s, sub-topic '%s' not added to '%s'".formatted(
                            loopPath, matchingTopic.getId(), parentId
                        );
                        parentTopic.getWarnings().add(warning);
                        logger.warn(warning);
                        continue;
                    }
                    topics.add(matchingTopic);
                }
            }
        } catch (IOException exception) {
            logger.warn("└ Failed to list directory: %s".formatted(directory));
        }

        // Sort topics by sortIndex within this directory
        topics.sort(Comparator.comparingInt(Topic::getSortIndex));
        return topics;
    }

    /**
     * Recursively collects all descendant topic IDs into the provided set.
     *
     * @param topic Topic to collect descendants from
     * @param ids   Set to collect IDs into
     */
    private void collectDescendantIds(Topic topic, Set<String> ids) {
        for (var child : topic.getTopics()) {
            ids.add(child.getId());
            collectDescendantIds(child, ids);
        }
    }

    @Override
    public String toString() {
        return "UniversalDocumentationLoader{" +
            "documentationType=" + documentationType +
            ", documentationIndexFile=" + documentationIndexFile +
            '}';
    }
}
