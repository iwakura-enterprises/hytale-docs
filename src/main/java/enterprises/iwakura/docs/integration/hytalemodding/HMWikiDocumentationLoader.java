package enterprises.iwakura.docs.integration.hytalemodding;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.hypixel.hytale.server.core.plugin.PluginManager;

import enterprises.iwakura.docs.api.hytalemodding.HMWikiApi;
import enterprises.iwakura.docs.api.hytalemodding.objects.HMWikiMod;
import enterprises.iwakura.docs.api.hytalemodding.objects.HMWikiMod.User;
import enterprises.iwakura.docs.api.hytalemodding.objects.HMWikiPage;
import enterprises.iwakura.docs.api.hytalemodding.response.HMWikiModListResponse;
import enterprises.iwakura.docs.api.hytalemodding.response.HMWikiModResponse;
import enterprises.iwakura.docs.api.hytalemodding.response.HMWikiPageContentResponse;
import enterprises.iwakura.docs.object.CacheIndex.Entry.CacheFileType;
import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.DocumentationType;
import enterprises.iwakura.docs.object.LoaderContext;
import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.docs.service.FileSystemCacheService;
import enterprises.iwakura.docs.service.loader.DocumentationLoader;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.docs.util.StringUtils;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class HMWikiDocumentationLoader extends DocumentationLoader {

    public static final String UNLOADED_INDEX_TOPIC_ID_PREFIX = "unloaded_index_";
    public static final String CACHE_FILE_MOD_LIST_NAME = "hytale_modding_wiki_mod_list";
    public static final String CACHE_FILE_MOD_PAGES_FORMAT = "hytale_modding_wiki_%s_pages";
    public static final String CACHE_FILE_MOD_CONTENT_FORMAT = "hytale_modding_wiki_%s_%s_content";

    private final FileSystemCacheService fileSystemCacheService;
    private final HMWikiApi hmWikiApi;
    private final Logger logger;

    private final Set<String> installedModNames = new HashSet<>();

    @Override
    public List<Documentation> load(LoaderContext loaderContext) {
        var logger = loaderContext.getLogger();

        installedModNames.clear();
        installedModNames.addAll(PluginManager.get().getPlugins().stream()
            .filter(plugin -> !plugin.getIdentifier().getGroup().equals("Hytale"))
            .map(plugin -> plugin.getIdentifier().getName()).toList());

        logger.info("└ Loading documentations from Hytale Modding Wiki...");

        HMWikiModListResponse modList;

        try {
            var loadedModList = fileSystemCacheService.loadByName(
                CACHE_FILE_MOD_LIST_NAME, CacheFileType.HYTALE_MODDING_WIKI_MOD_LIST, HMWikiModListResponse.class
            );

            if (loadedModList.isPresent() && !loadedModList.get().getData().hasError()) {
                logger.info("└ Loaded Hytale Modding Wiki mod list from file cache (fetched at %s)".formatted(
                    loadedModList.get().getEntry().getCreatedAt()
                ));
                modList = loadedModList.get().getData();
            } else {
                modList = hmWikiApi.fetchModList().send().join();
                if (modList.hasError()) {
                    throw new IllegalStateException(
                        "Failed to fetch response from HM Wiki: " + modList.getError());
                }
                // Gotta return .getMods() as GSON deserializes list of mods
                fileSystemCacheService.saveByName(CACHE_FILE_MOD_LIST_NAME, CacheFileType.HYTALE_MODDING_WIKI_MOD_LIST,
                    modList.getMods());
            }
        } catch (Exception exception) {
            logger.error("Failed to load mod list from HM Wiki", exception);
            return List.of();
        }

        logger.info("└ Loaded %d mods".formatted(modList.getMods().size()));

        return modList.getMods().stream()
            .map(mod -> {
                var documentation = Documentation.builder()
                    .type(isModInstalled(mod.getName()) ? DocumentationType.HYTALE_MODDING_WIKI_INSTALLED
                        : DocumentationType.HYTALE_MODDING_WIKI)
                    .group("voile_hm_wiki")
                    .id("%s_%s".formatted(mod.getSlug(), mod.getId()))
                    .name(mod.getName())
                    .build();
                documentation.getAdditionalInfo().setHytaleModdingWikiMod(mod);
                documentation.addTopics(createIndexTopic(documentation, mod));
                preloadDocumentation(documentation, mod, true);
                return documentation;
            }).toList();
    }

    private Topic createIndexTopic(Documentation documentation, HMWikiMod mod) {
        var topic = Topic.builder()
            .id(UNLOADED_INDEX_TOPIC_ID_PREFIX + mod.getSlug())
            .name(Optional.ofNullable(mod.getName()).orElse("Unknown mod"))
            .author(Optional.ofNullable(mod.getAuthor()).map(User::getName).orElse("Unknown author"))
            .description(Optional.ofNullable(mod.getDescription()).orElse("Hytale Modding Wiki"))
            .documentation(documentation)
            .markdownContent("If you see this, it means that the mod does not have any pages set up. Bummer!")
            .build();

        if (mod.getIndexPage() != null) {
            topic.setName(mod.getIndexPage().getTitle());
        } else {
            topic.setName("Open to load topics");
        }

        topic.setTopicOpenedCallback(context -> handleIndexTopicOpened(documentation, mod, context));

        return topic;
    }

    private void handleIndexTopicOpened(
        Documentation documentation,
        HMWikiMod mod,
        DocsContext context
    ) {
        var indexTopic = preloadDocumentation(documentation, mod, false);

        // If topic was just loaded, set it as the active topic
        if (indexTopic != null) {
            context.getInterfaceState().setTopic(indexTopic);
        }
    }

    private Topic createTopicFromModPage(
        Documentation documentation,
        HMWikiMod mod,
        HMWikiPage page
    ) {
        //logger.info("└ Creating topic for page " + page.getSlug());

        var topic = Topic.builder()
            .id(Optional.ofNullable(page.getSlug()).orElseGet(() -> UUID.randomUUID().toString()))
            .author(Optional.ofNullable(mod.getAuthor()).map(User::getName).orElse("Unknown author"))
            .name(Optional.ofNullable(page.getTitle()).orElse("Unnamed page"))
            .description(Optional.ofNullable(mod.getDescription()).orElse("Hytale Modding Wiki"))
            .documentation(documentation)
            .sortIndex(mod.getIndexPage() != null && Objects.equals(mod.getIndexPage().getId(), page.getId()) ? -1 : 0)
            .build();

        topic.setTopicOpenedCallback(context -> loadTopicPageContent(topic, mod, page));

        if (page.getChildren() != null) {
            var childTopics = page.getChildren().stream()
                .map(childPage -> createTopicFromModPage(documentation, mod, childPage))
                .toList();
            topic.addTopics(childTopics);
        }

        return topic;
    }

    private void loadTopicPageContent(
        Topic topic,
        HMWikiMod mod,
        HMWikiPage page
    ) {
        final var cacheFileName = CACHE_FILE_MOD_CONTENT_FORMAT.formatted(mod.getId(), page.getSlug());
        topic.setTopicOpenedCallback(null);

        try {
            var loadedPageContent = fileSystemCacheService.loadByName(
                cacheFileName, CacheFileType.HYTALE_MODDING_WIKI_PAGE_CONTENT, HMWikiPageContentResponse.class
            );

            if (loadedPageContent.isPresent() && !loadedPageContent.get().getData().hasError()) {
                // Content cached, load
                logger.info(
                    "Loaded page content for mod %s for page %s into topic %s from file system cache (created at %s)".formatted(
                        mod, page, topic, loadedPageContent.get().getEntry().getCreatedAt()
                    ));
                topic.setMarkdownContent(loadedPageContent.get().getData().getMarkdownContent());
            } else {
                logger.info("Loading page content for mod %s for page %s into topic %s from HM Wiki".formatted(
                    mod, page, topic
                ));

                var response = hmWikiApi.fetchPageContent(mod.getId(), page.getSlug()).send().join();
                if (response.hasError()) {
                    throw new IllegalStateException("Failed to fetch page content: " + response.getError());
                }

                topic.setMarkdownContent(response.getMarkdownContent());

                fileSystemCacheService.saveByName(cacheFileName, CacheFileType.HYTALE_MODDING_WIKI_PAGE_CONTENT,
                    response);
            }
        } catch (Exception exception) {
            logger.error("Failed to fetch page content for page %s for mod %s from HM Wiki!".formatted(
                page, mod
            ), exception);
            topic.setMarkdownContent(
                "<red>Failed to fetch content from Hytale Modding Wiki. See console for more info.</red>");
        }
    }

    /**
     * Preloads the documentation if required.
     *
     * @param documentation           Documentation
     * @param mod                     Hytale Modding Wiki Mod
     * @param onlyFromFileSystemCache If documentation should be preloaded only from file system cache. Used when
     *                                loading the mod documentations for the first time.
     *
     * @return If preloaded returns true, otherwise false.
     */
    public Topic preloadDocumentation(Documentation documentation, HMWikiMod mod, boolean onlyFromFileSystemCache) {
        if (documentation.countTopics() != 1
            || !documentation.getTopics().getFirst().getId().startsWith(UNLOADED_INDEX_TOPIC_ID_PREFIX)
            // Preload only installed mods
            || !isModInstalled(mod.getName())
        ) {
            return null;
        }

        final var cacheFileName = CACHE_FILE_MOD_PAGES_FORMAT.formatted(mod.getId());
        HMWikiModResponse modResponse;

        try {
            var loadedMod = fileSystemCacheService.loadByName(cacheFileName, CacheFileType.HYTALE_MODDING_WIKI_MOD,
                HMWikiModResponse.class);

            if (loadedMod.isPresent() && !loadedMod.get().getData().hasError()) {
                logger.info("Loaded topics for HM Wiki mod %s from file system storage (created at %s)".formatted(
                    mod, loadedMod.get().getEntry().getCreatedAt()
                ));
                modResponse = loadedMod.get().getData();
            } else {
                if (onlyFromFileSystemCache) {
                    return null;
                }
                logger.info("Loading topics for HM Wiki mod " + mod);

                modResponse = hmWikiApi.fetchMod(mod.getId()).send().join();
                if (modResponse.hasError()) {
                    throw new IllegalStateException("Failed to fetch pages: " + modResponse.getError());
                }
                fileSystemCacheService.saveByName(cacheFileName, CacheFileType.HYTALE_MODDING_WIKI_MOD, modResponse);
            }
        } catch (Exception exception) {
            logger.error("Failed to load pages for HM Wiki mod " + mod, exception);
            return null;
        }

        documentation.getTopics().clear();

        if (modResponse.getPages() == null || modResponse.getPages().isEmpty()) {
            logger.warn("Found no pages for HM Wiki mod " + mod);
            return null;
        }

        logger.info("Fetched %d pages from HM Wiki mod %s".formatted(modResponse.getPages().size(), mod));

        var topics = modResponse.getPages().stream()
            .map(modPage -> createTopicFromModPage(documentation, mod, modPage))
            .toList();
        documentation.addTopics(topics);

        // Load content into the index topic and replace the current one
        var page = Optional.ofNullable(mod.getIndexPage()).orElseGet(() -> modResponse.getPages().getFirst());
        var topicPage = createTopicFromModPage(documentation, mod, page);
        loadTopicPageContent(topicPage, mod, page);
        return topicPage;
    }

    /**
     * Checks if mod is installed
     *
     * @param modName Mod name
     *
     * @return True if yes, false otherwise
     */
    private boolean isModInstalled(String modName) {
        return installedModNames.stream()
            .anyMatch(installedModName -> StringUtils.isSimilar(installedModName, modName));
    }

    @Override
    public String toString() {
        return "HMWikiDocumentationLoader{}";
    }
}
