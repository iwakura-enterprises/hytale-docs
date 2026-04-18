package enterprises.iwakura.docs.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jspecify.annotations.NonNull;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import enterprises.iwakura.docs.components.Components;
import enterprises.iwakura.docs.config.DocsConfig;
import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.docs.object.InterfaceMode;
import enterprises.iwakura.docs.components.InterfacePreferencesComponent;
import enterprises.iwakura.docs.object.InterfaceState;
import enterprises.iwakura.docs.object.InternalTopic;
import enterprises.iwakura.docs.object.LocaleType;
import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.docs.ui.AboutVoilePage;
import enterprises.iwakura.docs.ui.DocumentationViewerPage;
import enterprises.iwakura.docs.ui.DocumentationViewerPage.PageData;
import enterprises.iwakura.docs.ui.LocaleTypeSelectorPage;
import enterprises.iwakura.docs.ui.render.DocumentationTreeRenderer;
import enterprises.iwakura.docs.ui.render.DocumentationViewerRenderer;
import enterprises.iwakura.docs.ui.render.DocumentationViewerRenderer.RenderData;
import enterprises.iwakura.docs.ui.render.TopicChapterTreeRenderer;
import enterprises.iwakura.docs.ui.render.TopicRenderer;
import enterprises.iwakura.docs.util.ChatInfo;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import io.github.insideranh.talemessage.TaleMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Bean
@RequiredArgsConstructor
public class DocumentationViewerService {

    private static final Map<UUID, InterfacePreferencesComponent> lastInterfacePreferencesForPlayer = Collections.synchronizedMap(new HashMap<>());
    private static final Executor RENDER_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r);
        thread.setUncaughtExceptionHandler((t, e) -> {
            HytaleLogger.get("Voile-Render").atSevere().withCause(e).log("Exception occurred in the render thread!");
            SentryService.captureException(e);
        });
        return thread;
    });

    private final ConfigurationService configurationService;
    private final ValidatorService validatorService;
    private final DocumentationService documentationService;
    private final DocumentationSearchService documentationSearchService;
    private final FallbackTopicService fallbackTopicService;
    private final DocumentationViewerRenderer documentationViewerRenderer;
    private final DocumentationTreeRenderer documentationTreeRenderer;
    private final TopicRenderer topicRenderer;
    private final TopicChapterTreeRenderer topicChapterTreeRenderer;
    private final RuntimeImageAssetService runtimeImageAssetService;
    private final DebugService debugService;
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
    public CompletableFuture<Boolean> openFor(PlayerRef playerRef, Optional<String> requestedTopicIdentifier, boolean notFoundTopicIfInvalid) {
        var documentations = documentationService.getEnabledDocumentations();
        var topicToOpen = Optional.<Topic>empty();

        Optional<InterfaceMode> switchToInterfaceMode = Optional.empty();
        var interfacePreferences = getInterfacePreferences(playerRef, null);
        var lastOpenedTopicIdentifier = Optional.ofNullable(interfacePreferences.getLastOpenedTopicIdentifier());

        // 1. Requested topic
        if (requestedTopicIdentifier.isPresent()) {
            topicToOpen = documentationSearchService.findTopic(playerRef, documentations, requestedTopicIdentifier.get(), null, interfacePreferences.getPreferredLocaleType());

            // Fallback to not found topic
            if (notFoundTopicIfInvalid && topicToOpen.isEmpty()) {
                topicToOpen = Optional.of(
                    fallbackTopicService.createTopicNotFound(documentations, requestedTopicIdentifier.get())
                );
            }
        }

        // 2. Last opened topic
        if (topicToOpen.isEmpty() && lastOpenedTopicIdentifier.isPresent()) {
            topicToOpen = documentationSearchService.findTopic(playerRef, documentations, lastOpenedTopicIdentifier.get(), null, interfacePreferences.getPreferredLocaleType());
        }

        // 3. Default config topic
        if (topicToOpen.isEmpty()) {
            var currentInterfaceMode = Optional.ofNullable(interfacePreferences.getLastInterfaceMode())
                .orElse(InterfaceMode.VOILE);
            switchToInterfaceMode = Optional.of(currentInterfaceMode);
            topicToOpen = documentationSearchService.findDefaultTopic(playerRef, documentations.stream()
                .filter(documentation -> currentInterfaceMode.has(documentation.getType()))
                .toList(), interfacePreferences.getPreferredLocaleType()
            );

            if (topicToOpen.isEmpty()) {
                topicToOpen = documentationSearchService.findDefaultTopic(playerRef, documentations, interfacePreferences.getPreferredLocaleType());

                if (topicToOpen.isPresent()) {
                    switchToInterfaceMode = InterfaceMode.forType(topicToOpen.get().getDocumentation().getType());
                }
            }
        }

        if (topicToOpen.isEmpty()) {
            ChatInfo.ERROR.send(playerRef, "Could not find a topic to open. (there is %d documentation(s))".formatted(
                documentations.size()
            ));
            return CompletableFuture.completedFuture(false);
        }

        var interfaceState = new InterfaceState(documentations, topicToOpen.get());
        switchToInterfaceMode.ifPresent(interfaceState::setInterfaceMode);
        return openFor(playerRef, DocsContext.of(playerRef, interfaceState));
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
    @SneakyThrows
    public CompletableFuture<Boolean> openFor(PlayerRef playerRef, DocsContext docsContext) {
        var docsConfig = configurationService.getDocsConfig();
        // Load docs context from preferences
        var interfacePreferences = getInterfacePreferences(playerRef, docsContext.getInterfaceState());
        docsContext.getInterfaceState().loadFromPreferences(interfacePreferences);
        docsContext.getInterfaceState().setFullTextSearch(docsConfig.isEnableFullTextSearch() && docsContext.getInterfaceState().isFullTextSearch());

        var docsContextRendered = DocsContext.of(docsContext);
        var ref = playerRef.getReference();

        if (ref == null) {
            ChatInfo.ERROR.send(playerRef, "PlayerRef's reference is null!");
            return CompletableFuture.completedFuture(false);
        }

        var store = ref.getStore();
        var player = store.getComponent(ref, Player.getComponentType());

        if (player == null) {
            ChatInfo.ERROR.send(playerRef, "Could not find Player component on PlayerRef!");
            return CompletableFuture.completedFuture(false);
        }

        var future = new CompletableFuture<Boolean>();

        RENDER_EXECUTOR.execute(() -> {
            String ui;

            docsContext.getTopic().invokeOpenedCallback(docsContext);

            try {
                ui = documentationViewerRenderer.render(docsContextRendered, new RenderData(docsContext.getDocumentations(), docsContext.getTopic()));
                docsContext.getCommandBuilder().appendInline(DocumentationViewerPage.MAIN_CONTENT_SELECTOR, ui);
            } catch (Exception exception) {
                logger.error("Failed to render DocumentationViewer!", exception);
                ChatInfo.ERROR.send(playerRef, "Failed to render Docs interface. See console for more information.");
                future.complete(false);
                return;
            }

            docsContextRendered.mergeInto(docsContext);

            if (!validatorService.validateUI(playerRef, docsContext, docsContext.getCommandBuilder())) {
                ChatInfo.ERROR.send(playerRef, "The generated UI for Docs is invalid. See console for more information.");
                future.complete(false);
                return;
            }

            runtimeImageAssetService.sendPendingAssets(playerRef);
            player.getPageManager().openCustomPage(ref, store, new DocumentationViewerPage(playerRef, this, logger, docsContext));
            future.complete(true);
        });

        return future;
    }

    /**
     * Replaces the topic content for page
     *
     * @param page    Page
     * @param context Context
     */
    @SneakyThrows
    public void replaceTopicContent(DocumentationViewerPage page, DocsContext context) {
        var playerRef = page.getPlayerRef();
        var player = page.getPlayer();

        if (player == null) {
            return;
        }

        var ref = playerRef.getReference();
        var store = ref.getStore();

        RENDER_EXECUTOR.execute(() -> {
            // Sve docs context to interface preferences, incl. currently open topic
            var interfacePreferences = getInterfacePreferences(playerRef, context.getInterfaceState());
            context.getInterfaceState().saveToPreferences(interfacePreferences);

            context.getTopic().invokeOpenedCallback(context);

            documentationTreeRenderer.clearAndAppendInline(context, context.getDocumentations());
            topicRenderer.clearAndAppendInline(context, context.getTopic());
            topicChapterTreeRenderer.clearAndAppendInline(context, context.getTopic());
            context.getCommandBuilder().set("#ContainerTitleGroup[0].Text", "Voile // " + context.getTopic().getName());

            if (!validatorService.validateUI(playerRef, context, context.getCommandBuilder())) {
                ChatInfo.ERROR.send(playerRef, "The generated UI for Docs is invalid. See console for more information.");
                player.getPageManager().setPage(ref, store, Page.None);
                return;
            }

            // Pending assets won't be loaded when simply updating the UI. Then entire UI
            // has to be updated (page closed and opened) in order for the assets to take an effect.
            // The pending assets will be cached, so it won't be re-loading them.
            if (runtimeImageAssetService.hasPendingAssets(playerRef)) {
                player.getWorld().execute(() -> openFor(playerRef, DocsContext.of(context)));
            } else {
                page.updateWithContext(context, false);
            }
        });
    }

    /**
     * Opens about voile page
     *
     * @param page Page
     */
    private void openAboutVoilePage(DocumentationViewerPage page) {
        var playerRef = page.getPlayerRef();
        var player = page.getPlayer();

        if (player == null) {
            return;
        }

        var ref = playerRef.getReference();
        var store = ref.getStore();

        player.getPageManager().openCustomPage(ref, store, new AboutVoilePage(playerRef, false, debugService));
    }

    /**
     * Opens locale type selector page
     *
     * @param page Page
     */
    private void openLocaleTypeSelectorPage(DocumentationViewerPage page) {
        var playerRef = page.getPlayerRef();
        var player = page.getPlayer();

        if (player == null) {
            return;
        }

        var ref = playerRef.getReference();
        var store = ref.getStore();

        player.getPageManager().openCustomPage(ref, store, new LocaleTypeSelectorPage(playerRef, this, documentationService, validatorService, logger, true));
    }

    /**
     * Updates only the topic chapter tree in the interface
     *
     * @param page    Page
     * @param context Context
     */
    public void updateDocumentationTree(DocumentationViewerPage page, DocsContext context) {
        var playerRef = page.getPlayerRef();
        var player = page.getPlayer();

        if (player == null) {
            return;
        }

        var ref = playerRef.getReference();
        var store = ref.getStore();

        RENDER_EXECUTOR.execute(() -> {
            documentationTreeRenderer.clearAndAppendInline(context, context.getDocumentations());

            if (!validatorService.validateUI(playerRef, context, context.getCommandBuilder())) {
                ChatInfo.ERROR.send(playerRef, "The generated UI for Docs is invalid. See console for more information.");
                player.getPageManager().setPage(ref, store, Page.None);
                return;
            }

            page.updateWithContext(context, false);
        });
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
        var state = docsContext.getInterfaceState();
        var action = data.getInterfaceAction();

        if (action == null) {
            logger.warn("Received page data without action! " + data);
            return;
        }

        switch (action) {
            case CHANGE_MODE -> {
                var currentMode = state.getInterfaceMode();
                var availableModes = documentationSearchService.getAvailableInterfaceModes(docsContext.getPlayerRef(), docsContext.getDocumentations());
                var nextMode = availableModes.stream()
                    .filter(mode -> currentMode != null && mode.ordinal() > currentMode.ordinal())
                    .findFirst()
                    .or(() -> availableModes.stream().findFirst())
                    .orElse(null);

                if (nextMode != null) {
                    var updatedDocsContext = DocsContext.of(docsContext);
                    state.setInterfaceMode(nextMode);
                    getInterfacePreferences(page.getPlayerRef(), state).setLastInterfaceMode(nextMode);
                    updateDocumentationTree(page, updatedDocsContext);
                }
            }
            case BACK -> {
                if (state.canGoBack()) {
                    openTopicByIdentifier(page, docsContext, state.getPreviousTopicAndMoveIndex());
                }
            }
            case FORWARD -> {
                if (state.canGoForward()) {
                    openTopicByIdentifier(page, docsContext, state.getNextTopicAndMoveIndex());
                }
            }
            case HOME -> {
                var documentations = docsContext.getDocumentations().stream()
                    .filter(documentation -> docsContext.getInterfaceState().getInterfaceMode() == null || docsContext.getInterfaceState().getInterfaceMode().has(documentation.getType()))
                    .toList();

                var defaultTopic = documentationSearchService.findDefaultTopic(docsContext.getPlayerRef(), documentations, state.getPreferredLocaleType());
                if (defaultTopic.isPresent()) {
                    state.resetHistory();
                    state.pushToHistory(defaultTopic.get(), true);
                    openTopicByIdentifier(page, docsContext, defaultTopic.get().getTopicIdentifier());
                } else {
                    ChatInfo.ERROR.send(page.getPlayerRef(), "There is no default topic to open!");
                }
            }
            case SEARCH -> {
                if (data.getTopicSearchQuery() != null) {
                    var updatedDocsContext = DocsContext.of(docsContext);
                    state.setTopicSearchQuery(data.getTopicSearchQuery());
                    getInterfacePreferences(page.getPlayerRef(), state).setLastTopicSearchQuery(data.getTopicSearchQuery());
                    updateDocumentationTree(page, updatedDocsContext);
                } else {
                    logger.error("PageData with SEARCH action without search query to open!");
                }
            }
            case OPEN_TOPIC -> {
                if (data.getOpenTopic() != null) {
                    var previousTopic = docsContext.getTopic();
                    var openedTopic = openTopicByIdentifier(page, docsContext, data.getOpenTopic());

                    if (!(openedTopic instanceof InternalTopic)) {
                        state.pushToHistory(openedTopic, true);
                    } else {
                        // Dirty easy hack...
                        // Allows the user to go back to the topic that caused to open internal topic
                        state.pushToHistory(previousTopic, false);
                    }
                } else {
                    logger.error("PageData with OPEN_TOPIC action without topic identifier to open!");
                }
            }
            case OPEN_ABOUT_VOILE_PAGE -> {
                openAboutVoilePage(page);
            }
            case OPEN_LOCALE_TYPE_SELECTOR_PAGE -> {
                openLocaleTypeSelectorPage(page);
            }
            case TOGGLE_FULL_TEXT_SEARCH -> {
                var updatedDocsContext = DocsContext.of(docsContext);
                if (!configurationService.getDocsConfig().isEnableFullTextSearch()) {
                    state.setFullTextSearch(false);
                } else {
                    state.setFullTextSearch(!state.isFullTextSearch());
                }
                getInterfacePreferences(page.getPlayerRef(), state).setFullTextSearch(state.isFullTextSearch());
                updateDocumentationTree(page, updatedDocsContext);
            }
            case SEND_CHAT_URL -> {
                if (data.getSendChatUrl() != null) {
                    var url = data.getSendChatUrl().startsWith("http")
                        ? data.getSendChatUrl()
                        : "https://" + data.getSendChatUrl();
                    var playerRef = page.getPlayerRef();
                    page.getPlayer().getPageManager().setPage(ref, store, Page.None);
                    playerRef.sendMessage(ChatInfo.INFO.of("Topic &e" + docsContext.getTopic().getName() + "{t} asks you to open a link: &e&l" + data.getSendChatUrl()).link(url));
                } else {
                    logger.error("PageData with SEND_CHAT_URL action without URL to send!");
                }
            }
            default -> logger.error("Invalid interface action specified in page data " + data);
        }
    }

    /**
     * Opens and returns topic specified by the topic identifier. If not found, returns {@link InternalTopic}
     *
     * @param page            Page
     * @param docsContext     Context
     * @param topicIdentifier Topic identifier
     *
     * @return Opened topic
     */
    private Topic openTopicByIdentifier(DocumentationViewerPage page, DocsContext docsContext, String topicIdentifier) {
        var updatedDocsContext = DocsContext.of(docsContext);
        var topic = documentationSearchService.findTopic(docsContext.getPlayerRef(), docsContext.getDocumentations(), topicIdentifier, docsContext.getTopic().getDocumentation(), docsContext.getInterfaceState().getPreferredLocaleType())
            .orElseGet(() -> fallbackTopicService.createTopicNotFound(docsContext.getDocumentations(), topicIdentifier));
        updatedDocsContext.getInterfaceState().setTopic(topic);
        InterfaceMode.forType(topic.getDocumentation().getType()).ifPresent(mode -> updatedDocsContext.getInterfaceState().setInterfaceMode(mode));
        replaceTopicContent(page, updatedDocsContext);
        return topic;
    }

    /**
     * Returns {@link InterfacePreferencesComponent} for specified player
     *
     * @param playerRef Player reference
     * @param defaultState Default state to load
     *
     * @return Never-null {@link InterfacePreferencesComponent}
     */
    public InterfacePreferencesComponent getInterfacePreferences(PlayerRef playerRef, InterfaceState defaultState) {
        return lastInterfacePreferencesForPlayer.computeIfAbsent(playerRef.getUuid(), k -> {
            var preferences = new InterfacePreferencesComponent();
            var state = Objects.requireNonNullElse(defaultState, new InterfaceState());
            if (defaultState == null) {
                state.loadFromDefaults(configurationService.getDocsConfig().getInterfacePreferencesDefaults());
            }
            // Set current checksum for newly created preferences (checksum is only on preferences, not on state)
            preferences.setChecksum(configurationService.getDocsConfig().getInterfacePreferencesDefaults().getChecksum());
            // Check if no preferred locale type is specified, default to player's current language
            if (state.getPreferredLocaleType() == null) {
                state.setPreferredLocaleType(LocaleType.fromHytaleLanguage(playerRef.getLanguage()));
            }
            state.saveToPreferences(preferences);
            return preferences;
        });
    }

    /**
     * Clears all in-memory interface preferences
     */
    public void clearPreferencesInMemory() {
        lastInterfacePreferencesForPlayer.clear();
    }

    /**
     * Clears preferences for specified player UUID in memory and within their store/holder
     *
     * @param playerUuid player UUID
     */
    public void clearPreferences(UUID playerUuid) {
        lastInterfacePreferencesForPlayer.remove(playerUuid);
        Optional.ofNullable(Universe.get().getPlayer(playerUuid))
            .ifPresentOrElse(playerRef -> {
                if (playerRef.getWorldUuid() != null) {
                    var world = Universe.get().getWorld(playerRef.getWorldUuid());
                    var ref = playerRef.getReference();

                    if (world != null && ref != null) {
                        var store = ref.getStore();
                        world.execute(() -> {
                            try {
                                if (store.getComponent(ref, Components.getInterfacePreferencesComponent()) != null) {
                                    store.removeComponent(ref, Components.getInterfacePreferencesComponent());
                                }
                            } catch (Exception exception) {
                                logger.error("Failed to remove interface preferences component from player's store " + playerUuid, exception);
                            }
                        });
                    }
                }
            }, () -> {
                Universe.get().getPlayerStorage().load(playerUuid).whenCompleteAsync((holder, exception) -> {
                    if (exception != null) {
                        logger.warn("Failed to load player %s from storage to reset their interface preferences!".formatted(playerUuid));
                        return;
                    }

                    try {
                        if (holder.getComponent(Components.getInterfacePreferencesComponent()) != null) {
                            holder.removeComponent(Components.getInterfacePreferencesComponent());
                            Universe.get().getPlayerStorage().save(playerUuid, holder);
                        }
                    } catch (Exception exceptionHolder) {
                        logger.error("Failed to remove interface preferences component from player's holder " + playerUuid, exceptionHolder);
                    }
                });
            });
    }

    /**
     * Loads interface preferences from player's holder
     *
     * @param playerRef Player reference
     * @param holder Holder for the player
     */
    public void loadInterfacePreferences(PlayerRef playerRef, Holder<EntityStore> holder) {
        var config = configurationService.getDocsConfig();
        if (config.isPersistInterfacePreferences()) {
            var interfacePreferences = holder.getComponent(Components.getInterfacePreferencesComponent());
            if (interfacePreferences != null) {
                if (Objects.equals(config.getInterfacePreferencesDefaults().getChecksum(), interfacePreferences.getChecksum())) {
                    lastInterfacePreferencesForPlayer.put(playerRef.getUuid(), interfacePreferences);
                    logger.info("Loaded interface preferences for player " + playerRef.getUuid());
                } else {
                    logger.warn("Ignoring older player's interface preferences due to checksum (%s)".formatted(interfacePreferences.getChecksum()));
                }
            }
        }
    }

    /**
     * Saves interface preferences to player's holder
     *
     * @param playerRef Player reference
     * @param holder Holder for the player
     */
    public void saveInterfacePreferences(PlayerRef playerRef, Holder<EntityStore> holder) {
        var config = configurationService.getDocsConfig();
        if (config.isPersistInterfacePreferences()) {
            var interfacePreferences = lastInterfacePreferencesForPlayer.get(playerRef.getUuid());

            if (interfacePreferences != null) {
                try {
                    holder.putComponent(Components.getInterfacePreferencesComponent(), interfacePreferences);
                    logger.info("Saved interface preferences for player " + playerRef.getUuid());
                } catch (Exception exception) {
                    logger.error("Failed to save interface preferences for player " + playerRef.getUuid(), exception);
                }
            }
        }
    }
}
