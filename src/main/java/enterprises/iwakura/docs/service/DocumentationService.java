package enterprises.iwakura.docs.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.gson.Gson;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

import enterprises.iwakura.docs.DocsAPI;
import enterprises.iwakura.docs.DocsPlugin;
import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.DocumentationType;
import enterprises.iwakura.docs.object.LoaderContext;
import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.docs.service.loader.DocumentationLoader;
import enterprises.iwakura.docs.service.loader.FileSystemDocumentationLoader;
import enterprises.iwakura.docs.service.loader.ResourcesDocumentationLoader;
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

    /**
     * Registers default documentation loaders
     */
    public void registerDocumentationLoaders() {
        logger.info("Registering default documentation loaders...");
        DocsAPI.get().register(plugin, new FileSystemDocumentationLoader(
            DocumentationType.SERVER,
            plugin.getDataDirectory().resolve(configurationService.getDocsConfig().getLoadDocumentationsFromDirectory()))
        );
        DocsAPI.get().register(plugin, new ResourcesDocumentationLoader(
            DocumentationType.INTERNAL,
            plugin.getClassLoader(),
            "internal-docs/index.json"
        ));
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

        var loaderContext = new LoaderContext(logger, gson, markdownService);

        for (var entry : documentationLoaders.entrySet()) {
            var plugin = entry.getKey();
            var loaders = entry.getValue();
            logger.info("Plugin %s specified %d documentation loader(s), loading...".formatted(
                plugin.getName(), loaders.size()
            ));
            for (DocumentationLoader documentationLoader : loaders) {
                try {
                    loadedDocumentations.addAll(documentationLoader.load(loaderContext));
                } catch (Exception exception) {
                    logger.error("Failed to load documentations registered by plugin %s with loader %s (%s)".formatted(
                        plugin.getName(), documentationLoader, documentationLoader.getClass()
                    ), exception);
                }
            }
        }

        logger.info("Loaded %d documentations".formatted(loadedDocumentations.size()));
    }

    /**
     * Returns unmodifiable list of enabled documentations filtered by the specified types
     *
     * @return Unmodifiable list of documentations
     */
    public List<Documentation> getDocumentations() {
        var docsConfig = configurationService.getDocsConfig();
        return loadedDocumentations.stream()
            .filter(documentation -> docsConfig.getEnabledTypes().contains(documentation.getType()))
            .sorted(Comparator.comparing((Documentation doc) -> doc.getType().ordinal())
                .thenComparing(Documentation::getSortIndex, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Documentation::getName))
            .toList();
    }

    /**
     * Returns default topic specified in documentation index. If none is specified, the first topic is returned.
     *
     * @return Optional topic (empty if no topics were loaded)
     */
    public Optional<Topic> getDefaultTopic() {
        var documentations = getDocumentations();
        if (documentations.isEmpty()) {
            return Optional.empty();
        }

        var docsConfig = configurationService.getDocsConfig();
        if (docsConfig.getDefaultTopicIdentifier() != null) {
            var defaultTopic = findTopic(documentations, docsConfig.getDefaultTopicIdentifier());
            if (defaultTopic.isPresent()) {
                return defaultTopic;
            }
        }

        // First found topic
        for (Documentation documentation : documentations) {
            Optional<Topic> firstTopic = documentation.getTopics().stream().findFirst();
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
     * @param documentations  Documentations to search in
     * @param topicIdentifier the topic identifier string, using colons as delimiters
     *
     * @return an {@link Optional} containing the found {@link Topic}, or empty if not found
     *
     * @see #findTopic(List, String, String, String, boolean)
     */
    public Optional<Topic> findTopic(List<Documentation> documentations, String topicIdentifier) {
        boolean documentationGroupOrId = false;
        String documentationGroup = null;
        String documentationId = null;
        String topicId = null;
        String[] openTopicData = topicIdentifier.split(":");

        if (openTopicData.length == 1) {
            topicId = openTopicData[0];
        } else if (openTopicData.length == 2) {
            documentationGroup = openTopicData[0];
            documentationId = openTopicData[0];
            topicId = openTopicData[1];
            documentationGroupOrId = true;
        } else if (openTopicData.length == 3) {
            documentationGroup = openTopicData[0];
            documentationId = openTopicData[1];
            topicId = openTopicData[2];
        }

        return findTopic(documentations, documentationGroup, documentationId, topicId, documentationGroupOrId);
    }

    /**
     * Finds topic specified by the parameters
     *
     * @param documentations         Documentations to search in
     * @param documentationGroup     Optional documentation group
     * @param documentationId        Optional documentation ID
     * @param topicId                Topic ID
     * @param documentationGroupOrId If search should be done loosely on documentation group / id
     *
     * @return Optional topic
     */
    public Optional<Topic> findTopic(
        List<Documentation> documentations, String documentationGroup,
        String documentationId,
        String topicId,
        boolean documentationGroupOrId
    ) {
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
            .findFirst();
    }
}
