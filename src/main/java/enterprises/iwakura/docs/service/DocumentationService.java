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
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;

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

    private final DocumentationSearchService documentationSearchService;
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
}
