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
        logger.info("└ Loading documentations from file system in directory " + documentationDirectory);
        var indexFile = documentationDirectory.resolve("index.json");

        if (!Files.exists(documentationDirectory)) {
            try {
                Files.createDirectories(documentationDirectory);
            } catch (IOException exception) {
                throw new RuntimeException("Failed to documentations directory: " + documentationDirectory, exception);
            }
        }

        if (!Files.exists(indexFile)) {
            logger.warn("└ index.json at %s does not exist! Creating new one...".formatted(indexFile));
            try {
                Files.writeString(indexFile, loaderContext.getGson().toJson(new DocumentationIndexConfig()));
            } catch (IOException exception) {
                throw new RuntimeException("Failed to create index.json at " + indexFile, exception);
            }
        }

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

        var documentations = new ArrayList<Documentation>();

        for (DocumentationConfig documentationConfig : indexConfig.getDocumentations()) {
            if (!documentationConfig.isEnabled()) {
                logger.warn("└ Skipping disabled " + documentationConfig);
                continue;
            }

            try {
                var documentation = loadDocumentation(loaderContext, documentationConfig);
                documentations.add(documentation);
                logger.info("└ Loaded %s with %d topics".formatted(
                    documentation, documentation.countTopics()
                ));
            } catch (Exception exception) {
                logger.error("└ Failed to load " + documentationConfig, exception);
            }
        }

        logger.info("└ Loaded %d documentations from %s".formatted(
            documentations.size(), indexFile
        ));

        return documentations;
    }

    protected Documentation loadDocumentation(LoaderContext loaderContext, DocumentationConfig documentationConfig) throws IOException {
        var logger = loaderContext.getLogger();
        var documentationRootDirectory = documentationDirectory.resolve(documentationConfig.getId());
        var documentation = documentationConfig.toDocumentation(documentationType);
        List<Path> markdownFiles;
        var pathToTopicConfig = new HashMap<Path, TopicConfig>();
        var pathToTopic = new HashMap<Path, Topic>();
        var topicMap = new HashMap<String, Topic>();
        var configMap = new HashMap<String, TopicConfig>();
        var topicIdToPath = new HashMap<String, Path>();
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
                    Files.readString(markdownFile, StandardCharsets.UTF_8)
                );
                pathToTopicConfig.put(markdownFile, topicConfig);
                configMap.put(topicConfig.getId(), topicConfig);
                topicIdToPath.put(topicConfig.getId(), markdownFile);
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
                topicMap.put(topic.getId(), topic);
            } catch (Exception exception) {
                logger.error("└ Invalid topic config, skipping %s (%s)".formatted(
                    file, topicConfig
                ));
            }
        }

        for (var topicConfig : configMap.values()) {
            var topic = topicMap.get(topicConfig.getId());
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
                    // Load all topics from directory recursively
                    var dirTopics = loadTopicsFromDirectory(potentialDir, pathToTopic, topicConfig.getId(), configMap, topic, logger);
                    for (var dirTopic : dirTopics) {
                        topic.getTopics().add(dirTopic);
                        childTopicIds.add(dirTopic.getId());
                    }
                    continue;
                }

                var subTopic = topicMap.get(subTopicId);
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

        var rootTopics = topicMap.values().stream()
            .filter(topic -> !childTopicIds.contains(topic.getId()))
            .sorted(Comparator.comparingInt(Topic::getSortIndex))
            .toList();
        documentation.addTopics(rootTopics);
        return documentation;
    }

    /**
     * Loads topics from a directory, treating direct .md files as sub-topics.
     * Does not load directories recursively.
     */
    protected List<Topic> loadTopicsFromDirectory(
        Path directory,
        Map<Path, Topic> pathToTopic,
        String parentId,
        Map<String, TopicConfig> configMap,
        Topic parentTopic,
        Logger logger
    ) {
        var topics = new ArrayList<Topic>();

        try (var stream = Files.list(directory)) {
            var entries = stream.toList();

            for (var entry : entries) {
                if (Files.isRegularFile(entry) && entry.toString().endsWith(".md")) {
                    // Find topic by matching the file path
                    var matchingTopic = pathToTopic.get(entry);
                    if (matchingTopic != null) {
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
                    } else {
                        logger.warn("└ No topic found for file: %s".formatted(entry));
                    }
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