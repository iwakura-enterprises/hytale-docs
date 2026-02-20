package enterprises.iwakura.docs.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import enterprises.iwakura.docs.config.DocsConfig;
import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.docs.ui.DocumentationViewerPage;
import enterprises.iwakura.docs.ui.DocumentationViewerPage.PageData;
import enterprises.iwakura.docs.ui.render.DocumentationTreeRenderer;
import enterprises.iwakura.docs.ui.render.DocumentationViewerRenderer;
import enterprises.iwakura.docs.ui.render.DocumentationViewerRenderer.RenderData;
import enterprises.iwakura.docs.ui.render.TopicChapterTreeRenderer;
import enterprises.iwakura.docs.ui.render.TopicRenderer;
import enterprises.iwakura.docs.util.ChatInfo;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class DocumentationViewerService {

    private static final Map<UUID, String> lastOpenedTopicForPlayer = Collections.synchronizedMap(new HashMap<>());

    private final ValidatorService validatorService;
    private final DocumentationService documentationService;
    private final FallbackTopicService fallbackTopicService;
    private final DocumentationViewerRenderer documentationViewerRenderer;
    private final DocumentationTreeRenderer documentationTreeRenderer;
    private final TopicRenderer topicRenderer;
    private final TopicChapterTreeRenderer topicChapterTreeRenderer;
    private final Logger logger;

    /**
     * Opens the interface for specified player.
     *
     * @param playerRef Player reference
     * @param requestedTopicIdentifier Topic identifier that should be opened, fallbacks to other if not found
     * @param notFoundTopicIfInvalid If 'Not Found' topic should be opened if the requested topic is not found
     *
     * @return True if opened, false otherwise
     */
    public boolean openFor(PlayerRef playerRef, Optional<String> requestedTopicIdentifier, boolean notFoundTopicIfInvalid) {
        var documentations = documentationService.getDocumentations();
        var topicToOpen = Optional.<Topic>empty();

        var lastOpenedTopicIdentifier = Optional.ofNullable(lastOpenedTopicForPlayer.get(playerRef.getUuid()));

        // 1. Requested topic
        if (requestedTopicIdentifier.isPresent()) {
            topicToOpen = documentationService.findTopic(documentations, requestedTopicIdentifier.get());

            // Fallback to not found topic
            if (notFoundTopicIfInvalid && topicToOpen.isEmpty()) {
                topicToOpen = Optional.of(
                    fallbackTopicService.createTopicNotFound(documentations, requestedTopicIdentifier.get())
                );
            }
        }

        // 2. Last opened topic
        if (topicToOpen.isEmpty() && lastOpenedTopicIdentifier.isPresent()) {
            topicToOpen = documentationService.findTopic(documentations, lastOpenedTopicIdentifier.get());
        }

        // 3. Default config topic
        if (topicToOpen.isEmpty()) {
            topicToOpen = documentationService.getDefaultTopic(documentations);
        }

        if (topicToOpen.isEmpty()) {
            ChatInfo.ERROR.send(playerRef, "Could not find a topic to open. (there is %d documentation(s))".formatted(
                documentations.size()
            ));
            return false;
        }

        return openFor(playerRef, DocsContext.of(
            documentations,
            topicToOpen.get()
        ));
    }

    /**
     * Opens Docs interface for player. Validates the generated UI based on {@link DocsConfig.Validator}.
     * Allows to open the interface even if docsConfig.enabled == false
     *
     * @param playerRef   Player ref
     * @param docsContext Docs context
     *
     * @return True if Docs interfaces was opened, false otherwise
     */
    public boolean openFor(PlayerRef playerRef, DocsContext docsContext) {
        lastOpenedTopicForPlayer.put(playerRef.getUuid(), docsContext.getTopic().getTopicIdentifier());
        var docsContextRendered = DocsContext.of(docsContext);
        String ui;

        try {
            ui = documentationViewerRenderer.render(docsContextRendered, new RenderData(docsContext.getDocumentations(), docsContext.getTopic()));
            docsContext.getCommandBuilder().appendInline(DocumentationViewerPage.MAIN_CONTENT_SELECTOR, ui);
        } catch (Exception exception) {
            logger.error("Failed to render DocumentationViewer!", exception);
            ChatInfo.ERROR.send(playerRef, "Failed to render Docs interface. See console for more information.");
            return false;
        }

        docsContextRendered.mergeInto(docsContext);

        if (!validatorService.validateUI(playerRef, docsContext, docsContext.getCommandBuilder())) {
            ChatInfo.ERROR.send(playerRef, "The generated UI for Docs is invalid. See console for more information.");
            return false;
        }

        var ref = playerRef.getReference();

        if (ref == null) {
            ChatInfo.ERROR.send(playerRef, "PlayerRef's reference is null!");
            return false;
        }

        var store = ref.getStore();
        var player = store.getComponent(ref, Player.getComponentType());

        if (player == null) {
            ChatInfo.ERROR.send(playerRef, "Could not find Player component on PlayerRef!");
            return false;
        }

        player.getPageManager().openCustomPage(ref, store, new DocumentationViewerPage(playerRef, this, docsContext));
        return true;
    }

    /**
     * Replaces the topic content for page
     *
     * @param page    Page
     * @param context Context
     */
    public void replaceTopicContent(DocumentationViewerPage page, DocsContext context) {
        documentationTreeRenderer.clearAndAppendInline(context, context.getDocumentations());
        topicRenderer.clearAndAppendInline(context, context.getTopic());
        topicChapterTreeRenderer.clearAndAppendInline(context, context.getTopic());
        context.getCommandBuilder().set("#ContainerTitleGroup[0].Text", "Voile // " + context.getTopic().getName());

        var playerRef = page.getPlayerRef();
        if (!validatorService.validateUI(playerRef, context, context.getCommandBuilder())) {
            ChatInfo.ERROR.send(playerRef, "The generated UI for Docs is invalid. See console for more information.");

            var ref = playerRef.getReference();
            if (ref != null) {
                var store = ref.getStore();
                var player = store.getComponent(ref, Player.getComponentType());
                if (player != null) {
                    player.getPageManager().setPage(ref, store, Page.None);
                }
            }
            return;
        }

        lastOpenedTopicForPlayer.put(playerRef.getUuid(), context.getTopic().getTopicIdentifier());
        page.replaceWithContext(context, false);
    }

    /**
     * Handles page data event
     *
     * @param page        Page
     * @param ref         Ref
     * @param store       Store
     * @param docsContext Current Docs Context
     * @param data        Data
     */
    public void handlePageData(
        DocumentationViewerPage page,
        Ref<EntityStore> ref,
        Store<EntityStore> store,
        DocsContext docsContext,
        PageData data
    ) {
        if (data.getOpenTopic() != null) {
            var updatedDocsContext = DocsContext.of(docsContext);
            updatedDocsContext.setTopic(documentationService.findTopic(docsContext.getDocumentations(), data.getOpenTopic())
                .orElseGet(() -> fallbackTopicService.createTopicNotFound(docsContext.getDocumentations(), data.getOpenTopic()))
            );
            replaceTopicContent(page, updatedDocsContext);
        }
    }
}
