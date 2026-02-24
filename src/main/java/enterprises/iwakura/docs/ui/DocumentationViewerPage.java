package enterprises.iwakura.docs.ui;

import org.jspecify.annotations.NonNull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.docs.service.DocumentationViewerService;
import enterprises.iwakura.docs.ui.DocumentationViewerPage.PageData;
import enterprises.iwakura.docs.util.Logger;
import lombok.Data;

public class DocumentationViewerPage extends InteractiveCustomUIPage<PageData> {

    public static final String MAIN_CONTENT_SELECTOR = "#Content";

    private final DocumentationViewerService documentationViewerService;
    private final Logger logger;
    private DocsContext docsContext;

    public DocumentationViewerPage(
        PlayerRef playerRef,
        DocumentationViewerService documentationViewerService,
        Logger logger,
        DocsContext docsContext
    ) {
        super(playerRef, CustomPageLifetime.CanDismiss, PageData.CODEC);
        this.documentationViewerService = documentationViewerService;
        this.logger = logger;
        this.docsContext = docsContext;
    }

    @Override
    public void build(
        @NonNull Ref<EntityStore> ref,
        @NonNull UICommandBuilder commandBuilder,
        @NonNull UIEventBuilder eventBuilder,
        @NonNull Store<EntityStore> store
    ) {
        // Base page UI
        commandBuilder.append("Docs/Pages/DocumentationViewerPage.ui");
        docsContext.mergeInto(commandBuilder, eventBuilder);
    }

    /**
     * Replaces the content using the {@link DocsContext}
     *
     * @param context UI Context
     * @param clear   If the interface should be cleared
     */
    public void updateWithContext(DocsContext context, boolean clear) {
        docsContext = context;
        sendUpdate(context.getCommandBuilder(), context.getEventBuilder(), clear);
    }

    @Override
    public void handleDataEvent(
        @NonNull Ref<EntityStore> ref,
        @NonNull Store<EntityStore> store,
        DocumentationViewerPage.@NonNull PageData data
    ) {
        documentationViewerService.handlePageData(this, ref, store, docsContext, data);
    }

    public PlayerRef getPlayerRef() {
        return playerRef;
    }

    /**
     * Gets player if their ref is valid and they are in world
     *
     * @return Nullable player
     */
    public Player getPlayer() {
        var ref = playerRef.getReference();

        if (ref == null || !ref.isValid()) {
            logger.error("Player's Ref %s is null/invalid, cannot replace topic content".formatted(
                playerRef.getUuid()
            ));
            return null;
        }

        var store = ref.getStore();
        var player = store.getComponent(ref, Player.getComponentType());

        if (player == null) {
            logger.error("Cannot find Player component on player ref %s".formatted(
                playerRef.getUuid()
            ));
            return null;
        }

        if (player.getWorld() == null) {
            logger.error("Player %s is in no world!".formatted(
                playerRef.getUuid()
            ));
            return null;
        }
        return player;
    }

    @Data
    public static class PageData {

        public static final String OPEN_TOPIC_FIELD = "OpenTopic";
        public static final String TOPIC_SEARCH_QUERY_FIELD = "@TopicSearchQuery";

        public static final BuilderCodec<PageData> CODEC = BuilderCodec.builder(PageData.class, PageData::new)
            .append(new KeyedCodec<>(OPEN_TOPIC_FIELD, Codec.STRING), PageData::setOpenTopic, PageData::getOpenTopic).add()
            .append(new KeyedCodec<>(TOPIC_SEARCH_QUERY_FIELD, Codec.STRING), PageData::setTopicSearchQuery, PageData::getTopicSearchQuery).add()
            .build();

        private String openTopic;
        private String topicSearchQuery;
    }
}
