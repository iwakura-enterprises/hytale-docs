package enterprises.iwakura.docs.service.loader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;

import enterprises.iwakura.docs.config.AssetDocumentationConfig;
import enterprises.iwakura.docs.config.AssetDocumentationIndexConfig;
import enterprises.iwakura.docs.config.TopicConfig;
import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.DocumentationType;
import enterprises.iwakura.docs.object.LoaderContext;
import enterprises.iwakura.docs.object.Topic;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * Provides a way to register documentations from specified classloader's resources.
 * </p>
 * <p>
 * This documentation loader takes the desired {@link DocumentationType} to load the documentation as,
 * the {@link ClassLoader} (preferably your plugin's class loader, see {@link JavaPlugin#getClassLoader()}) and
 * the full path to <code>index.json</code> within your plugin's resources.
 * </p>
 * <h3>index.json structure</h3>
 * <p>
 * The <code>index.json</code> mut follow specific structure to be correctly loaded:
 * </p>
 * <pre>
 * {
 *   "documentation": {
 *     "group": "MyGroup",
 *     "id": "MyDocumentation",
 *     "name": "My documentations",
 *     "enabled": true,
 *     "sortIndex": 1
 *   },
 *   "topics": [
 *     {
 *       "file": "index.md"
 *     },
 *     {
 *       "file": "tutorials.md",
 *       "subTopics": [
 *         {
 *           "file": "tutorials/tutorial-01.md"
 *         },
 *         {
 *           "file": "tutorials/tutorial-02.md"
 *         }
 *       ]
 *     }
 *   ]
 * }
 * </pre>
 * <p>
 * With the following resources structure.
 * </p>
 * <pre>
 * src/main/resources/
 *   my-docs/
 *      index.json
 *      index.md
 *      tutorials.md
 *      tutorials/
 *          tutorial-01.md
 *          tutorial-02.md
 * </pre>
 * <p>
 *     <ul>
 *         <li>
 *             The documentation object. This specifies the information about the documentation, like its group, id,
 *             name, if it's enabled and the sortIndex within its {@link DocumentationType} group.
 *         </li>
 *         <li>
 *             List of topics within the documentation. Each entry defines one topic with one or more sub-topics.
 *             <ul>
 *                 <li>
 *                      The topic's <code>file</code> <b>must</b> contain the full path to the Markdown file from the
 *                      documentation's root (here <code>my-docs</code>). Otherwise the markdown file won't be found by the
 *                      loader and thus won't be loaded. When the documentation loader reads the resource files, it will use
 *                      the directory where the <code>index.json</code> is located.
 *                 </li>
 *                 <li>
 *                      The Markdown's file is used as fallback topic ID, if no ID is specified
 *                      in Markdown's front-matter (e.g. <code>tutorials/tutorial-01-md</code> becomes topic ID
 *                      <code>tutorial-01</code> with full identification of <code>MyGroup:MyDocumentation:tutorial-01</code>).
 *                 </li>
 *             </ul>
 *         </li>
 *         <li>
 *             The <code>index.json</code> would be referenced as <code>my-docs/index.json</code>
 *         </li>
 *     </ul>
 * </p>
 *
 */
@RequiredArgsConstructor
public class ResourcesDocumentationLoader extends DocumentationLoader {

    private final DocumentationType documentationType;
    private final ClassLoader classLoader;
    private final String indexPath;

    @Override
    public List<Documentation> load(LoaderContext loaderContext) {
        var logger = loaderContext.getLogger();
        logger.info("Loading documentations from class loader resources at " + indexPath);

        String indexContent;
        try (var inputStream = classLoader.getResourceAsStream(indexPath)) {
            if (inputStream == null) {
                logger.error("Documentations index.json not found in resources: " + indexPath);
                return List.of();
            }
            indexContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            logger.error("Failed to read index.json from resources: " + indexPath, exception);
            return List.of();
        }

        AssetDocumentationIndexConfig indexConfig;
        try {
            indexConfig = loaderContext.getGson().fromJson(indexContent, AssetDocumentationIndexConfig.class);
        } catch (Exception exception) {
            logger.error("Failed to parse index.json: " + indexPath, exception);
            return List.of();
        }

        if (indexConfig.getDocumentations() == null) {
            logger.error("Null documentations in index: " + indexPath);
            return List.of();
        }

        return indexConfig.getDocumentations().stream()
            .map(documentationConfig -> {
                if (!documentationConfig.isEnabled()) {
                    logger.warn("Skipping disabled " + documentationConfig);
                    return null;
                }

                logger.info("Documentation index %s defines %d root topics".formatted(
                    indexPath, documentationConfig.getTopics().size()
                ));

                try {
                    var documentation = loadDocumentation(loaderContext, documentationConfig);
                    logger.info("Loaded %s with %d topics".formatted(
                        documentation, documentation.countTopics()
                    ));
                    return documentation;
                } catch (Exception exception) {
                    logger.error("Failed to load " + documentationConfig, exception);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toList();
    }

    private Documentation loadDocumentation(
        LoaderContext loaderContext,
        AssetDocumentationConfig documentationConfig
    ) throws IOException {
        var logger = loaderContext.getLogger();
        var documentation = documentationConfig.toDocumentation(documentationType);
        var basePath = getBasePath() + "/" + documentationConfig.getGroup() + "_" + documentationConfig.getId();
        var fileToTopic = new HashMap<String, Topic>();
        var configMap = new HashMap<String, TopicConfig>();

        // Load all topics recursively from the index
        loadTopicEntries(documentation, loaderContext, documentationConfig.getTopics(), basePath, fileToTopic, configMap);

        logger.info("Loaded %d topics from resources".formatted(fileToTopic.size()));

        // Build the topic hierarchy
        var rootTopics = buildTopicHierarchy(loaderContext, documentationConfig.getTopics(), fileToTopic, configMap);

        documentation.addTopics(rootTopics);
        return documentation;
    }

    private void loadTopicEntries(
        Documentation documentation,
        LoaderContext loaderContext,
        List<AssetDocumentationConfig.Entry> entries,
        String basePath,
        Map<String, Topic> fileToTopic,
        Map<String, TopicConfig> configMap
    ) throws IOException {
        var logger = loaderContext.getLogger();

        for (var entry : entries) {
            var filePath = basePath + "/" + entry.getFile();
            var topicId = entry.getFile()
                .substring(entry.getFile().lastIndexOf('/') + 1)
                .replace(".md", "");

            try (var inputStream = classLoader.getResourceAsStream(filePath)) {
                if (inputStream == null) {
                    logger.warn("Topic file not found in resources: " + filePath);
                    continue;
                }

                var content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                var topicConfig = loaderContext.getMarkdownService().readMarkdownAsTopicConfig(
                    topicId,
                    content
                );
                configMap.put(topicConfig.getId(), topicConfig);

                var topic = topicConfig.toTopic(documentation);
                fileToTopic.put(entry.getFile(), topic);
            } catch (Exception exception) {
                throw new IOException("Failed to load topic file: " + filePath, exception);
            }

            // Recursively load sub-topic entries
            if (entry.getSubTopics() != null && !entry.getSubTopics().isEmpty()) {
                loadTopicEntries(documentation, loaderContext, entry.getSubTopics(), basePath, fileToTopic, configMap);
            }
        }
    }

    private List<Topic> buildTopicHierarchy(
        LoaderContext loaderContext,
        List<AssetDocumentationConfig.Entry> entries,
        Map<String, Topic> fileToTopic,
        Map<String, TopicConfig> configMap
    ) {
        var logger = loaderContext.getLogger();
        var result = new ArrayList<Topic>();

        for (var entry : entries) {
            var topic = fileToTopic.get(entry.getFile());
            if (topic == null) {
                logger.warn("No topic found for entry: " + entry.getFile());
                continue;
            }

            // Add sub-topics
            if (entry.getSubTopics() != null && !entry.getSubTopics().isEmpty()) {
                var subTopics = buildTopicHierarchy(loaderContext, entry.getSubTopics(), fileToTopic, configMap);
                for (var subTopic : subTopics) {
                    // Check for circular reference
                    var visitedPath = new ArrayList<String>();
                    if (hasCircularReference(topic.getId(), subTopic.getId(), configMap, visitedPath)) {
                        var loopPath = String.join(" -> ", visitedPath) + " -> " + visitedPath.getFirst();
                        var warning = "Topic loop detected: %s, sub-topic '%s' not added to '%s'".formatted(
                            loopPath, subTopic.getId(), topic.getId()
                        );
                        topic.getWarnings().add(warning);
                        logger.warn(warning);
                        continue;
                    }
                    topic.getTopics().add(subTopic);
                }
            }

            result.add(topic);
        }

        return result;
    }

    private String getBasePath() {
        var lastSlash = indexPath.lastIndexOf('/');
        return lastSlash > 0 ? indexPath.substring(0, lastSlash) : "";
    }
}