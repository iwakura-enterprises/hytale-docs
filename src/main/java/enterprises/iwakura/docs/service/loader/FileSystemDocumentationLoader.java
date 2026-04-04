package enterprises.iwakura.docs.service.loader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import enterprises.iwakura.docs.config.DocumentationConfig;
import enterprises.iwakura.docs.config.DocumentationIndexConfig;
import enterprises.iwakura.docs.config.TopicConfig;
import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.DocumentationType;
import enterprises.iwakura.docs.object.LoaderContext;
import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.docs.util.Logger;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileSystemDocumentationLoader extends DocumentationLoader {

    protected final DocumentationType documentationType;
    protected final Path documentationDirectory;

    @Override
    public List<Documentation> load(LoaderContext loaderContext) {
        var logger = loaderContext.getLogger();
        var documentations = new ArrayList<Documentation>();

        logger.info("└ Loading documentations from in directory " + documentationDirectory);
        var indexFile = documentationDirectory.resolve("index.json");

        // ensure exists
        if (!Files.exists(documentationDirectory)) {
            try {
                Files.createDirectories(documentationDirectory);
            } catch (IOException exception) {
                throw new RuntimeException("Failed to documentations directory: " + documentationDirectory, exception);
            }
        }

        // ensure exists
        if (!Files.exists(indexFile)) {
            logger.warn("└ index.json at %s does not exist! Creating new one...".formatted(indexFile));
            try {
                Files.writeString(indexFile, loaderContext.getGson().toJson(new DocumentationIndexConfig()));
            } catch (IOException exception) {
                throw new RuntimeException("Failed to create index.json at " + indexFile, exception);
            }
        }

        // Load documentation index config
        DocumentationIndexConfig indexConfig;
        try {
            indexConfig = loaderContext.getGson().fromJson(Files.readString(indexFile), DocumentationIndexConfig.class);
        } catch (Exception exception) {
            logger.error("└ Failed to load index.json: %s" + indexFile, exception);
            return List.of();
        }
        logger.info("└ Documentation index %s defines %d documentations".formatted(
            indexFile, indexConfig.getDocumentations().size()
        ));

        // Go thru documentations defined in the config
        for (DocumentationConfig documentationConfig : indexConfig.getDocumentations()) {
            if (!documentationConfig.isEnabled()) {
                logger.warn("└ Skipping disabled " + documentationConfig);
                continue;
            }

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
        logger.info("└ Loaded %d documentations from %s".formatted(documentations.size(), indexFile));

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
    protected Documentation loadDocumentation(LoaderContext loaderContext, DocumentationConfig documentationConfig)
        throws IOException {
        var logger = loaderContext.getLogger();
        var documentationRootDirectory = documentationDirectory.resolve(documentationConfig.getId());
        var documentation = documentationConfig.toDocumentation(documentationType);
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

        if (!Files.exists(documentationRootDirectory)) {
            logger.warn("└ Directory %s does not exist, creating..." + documentationRootDirectory);
            Files.createDirectories(documentationRootDirectory);
        }

        try (var pathStream = Files.walk(documentationRootDirectory)) {
            markdownFiles = pathStream.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".md"))
                .toList();
        } catch (IOException exception) {
            throw new IOException("Failed to read documentation root directory %s for topics!".formatted(
                documentationRootDirectory
            ), exception);
        }

        logger.info("└ Found %d markdown files in %s".formatted(
            markdownFiles.size(), documentationRootDirectory
        ));

        for (var markdownFile : markdownFiles) {
            try {
                var topicConfig = loaderContext.getMarkdownService().readMarkdownAsTopicConfig(
                    markdownFile.getFileName().toString(),
                    Files.readString(markdownFile, StandardCharsets.UTF_8),
                    loaderContext.getConfigurationService().getDocsConfig().getInterfacePreferencesDefaults().getLocaleType()
                );
                pathToTopicConfig.put(markdownFile, topicConfig);

                // configMap holds one (primary) config per base id for sub-topic / circular-reference resolution.
                // We keep the config with the lowest locale ordinal so that sub-topic lists are resolved
                // from the primary locale's perspective.
                var baseId = topicConfig.getId();
                var existing = configMap.get(baseId);
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
                topic.setTopicFilePath(file);
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
            var parentDir = parentFilePath != null ? parentFilePath.getParent() : documentationRootDirectory;

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

    @Override
    public String toString() {
        return "FileSystemDocumentationLoader{" +
            "documentationType=" + documentationType +
            ", documentationDirectory=" + documentationDirectory +
            '}';
    }
}