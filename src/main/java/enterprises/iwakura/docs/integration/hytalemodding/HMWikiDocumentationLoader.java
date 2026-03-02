package enterprises.iwakura.docs.integration.hytalemodding;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import enterprises.iwakura.docs.api.hytalemodding.HMWikiApi;
import enterprises.iwakura.docs.api.hytalemodding.objects.HMWikiMod;
import enterprises.iwakura.docs.api.hytalemodding.objects.HMWikiPage;
import enterprises.iwakura.docs.api.hytalemodding.response.HMWikiModListResponse;
import enterprises.iwakura.docs.api.hytalemodding.response.HMWikiModResponse;
import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.DocumentationType;
import enterprises.iwakura.docs.object.LoaderContext;
import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.docs.service.loader.DocumentationLoader;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HMWikiDocumentationLoader extends DocumentationLoader {

    public static final String UNLOADED_INDEX_TOPIC_ID_PREFIX = "unloaded_index_";

    private final HMWikiService hmWikiService;
    private final HMWikiApi hmWikiApi;

    @Override
    public List<Documentation> load(LoaderContext loaderContext) {
        var logger = loaderContext.getLogger();
        logger.info("└ Loading documentations from Hytale Modding Wiki...");

        HMWikiModListResponse modList;

        try {
            modList = hmWikiApi.fetchModList().send().join();
            if (modList.hasError()) {
                throw new IllegalStateException(
                    "Failed to fetch response from HM Wiki: " + modList.getError());
            }
        } catch (Exception exception) {
            logger.error("Failed to load mod list from HM Wiki", exception);
            return List.of();
        }

        logger.info("└ Fetched %d mods".formatted(modList.getMods().size()));

        return modList.getMods().stream()
            .map(mod -> {
                var documentation = Documentation.builder()
                    .type(DocumentationType.HYTALE_MODDING_WIKI)
                    .group("voile_hm_wiki")
                    .id("hm_wiki_%s_%s".formatted(mod.getSlug(), mod.getId()))
                    .name(mod.getName())
                    .build();
                documentation.addTopics(createIndexTopic(loaderContext, documentation, mod));
                return documentation;
            }).toList();
    }

    private Topic createIndexTopic(LoaderContext loaderContext, Documentation documentation, HMWikiMod mod) {
        var topic = Topic.builder()
            .id(UNLOADED_INDEX_TOPIC_ID_PREFIX + mod.getSlug())
            .author("N/A") // TODO: Ask Neil to add this to the API
            .description(mod.getDescription())
            .documentation(documentation)
            .markdownContent("If you see this, it means that the mod does not have any pages set up. Bummer!")
            .build();

        if (mod.getIndexPage() != null) {
            topic.setName(mod.getIndexPage().getTitle());
        } else {
            topic.setName("Open to load topics");
        }

        topic.setTopicOpenedCallback(context -> handleIndexTopicOpened(loaderContext, documentation, mod, context));

        return topic;
    }

    private void handleIndexTopicOpened(
        LoaderContext loaderContext,
        Documentation documentation,
        HMWikiMod mod,
        DocsContext context
    ) {
        var logger = loaderContext.getLogger();

        // Mod was already loaded, skip
        if (documentation.countTopics() != 1 || !documentation.getTopics().getFirst().getId()
            .startsWith(UNLOADED_INDEX_TOPIC_ID_PREFIX)) {
            return;
        }

        logger.info("Loading topics for HM Wiki mod " + mod);
        HMWikiModResponse modResponse;

        try {
            modResponse = hmWikiApi.fetchMod(mod.getId()).send().join();
            if (modResponse.hasError()) {
                throw new IllegalStateException("Failed to fetch pages: " + modResponse.getError());
            }
        } catch (Exception exception) {
            logger.error("Failed to load pages for HM Wiki mod " + mod, exception);
            return;
        }

        documentation.getTopics().clear();

        if (modResponse.getPages() == null || modResponse.getPages().isEmpty()) {
            logger.warn("Found no pages for HM Wiki mod " + mod);
            return;
        }

        logger.info("Fetched %d pages from HM Wiki mod %s".formatted(modResponse.getPages().size(), mod));

        var topics = modResponse.getPages().stream()
            .map(modPage -> createTopicFromModPage(loaderContext, documentation, mod, modPage))
            .toList();
        documentation.addTopics(topics);

        // Load content into the index topic and replace the current one
        var page = Optional.ofNullable(mod.getIndexPage()).orElseGet(() -> modResponse.getPages().getFirst());
        var topicPage = createTopicFromModPage(loaderContext, documentation, mod, page);
        loadTopicPageContent(topicPage, loaderContext, mod, page);
        context.getInterfaceState().setTopic(topicPage);
    }

    private Topic createTopicFromModPage(
        LoaderContext loaderContext,
        Documentation documentation,
        HMWikiMod mod,
        HMWikiPage page
    ) {
        var logger = loaderContext.getLogger();
        logger.info("└ Creating topic for page " + page.getSlug());

        var topic = Topic.builder()
            .id(page.getSlug())
            .author("N/A")
            .name(page.getTitle())
            .description(mod.getDescription())
            .documentation(documentation)
            .sortIndex(mod.getIndexPage() != null && Objects.equals(mod.getIndexPage().getId(), page.getId()) ? -1 : 0)
            .build();

        topic.setTopicOpenedCallback(context -> loadTopicPageContent(topic, loaderContext, mod, page));

        if (page.getChildren() != null) {
            var childTopics = page.getChildren().stream()
                .map(childPage -> createTopicFromModPage(loaderContext, documentation, mod, childPage))
                .toList();
            topic.addTopics(childTopics);
        }

        return topic;
    }

    private void loadTopicPageContent(
        Topic topic,
        LoaderContext loaderContext,
        HMWikiMod mod,
        HMWikiPage page
    ) {
        var logger = loaderContext.getLogger();
        topic.setTopicOpenedCallback(null);

        logger.info("Loading page content for mod %s for page %s into topic %s from HM Wiki".formatted(
            mod, page, topic
        ));

        try {
            var response = hmWikiApi.fetchPageContent(mod.getId(), page.getSlug()).send().join();
            if (response.hasError()) {
                throw new IllegalStateException("Failed to fetch page content: " + response.getError());
            }

            topic.setMarkdownContent(response.getMarkdownContent());
        } catch (Exception exception) {
            logger.error("Failed to fetch page content for page %s for mod %s from HM Wiki!".formatted(
                page, mod
            ), exception);
            topic.setMarkdownContent("<red>Failed to fetch content from Hytale Modding Wiki. See console for more info.</red>");
        }
    }

    @Override
    public String toString() {
        return "HMWikiDocumentationLoader{}";
    }
}
