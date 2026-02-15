package enterprises.iwakura.docs.ui;

import org.jspecify.annotations.NonNull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.docs.service.DocumentationViewerService;
import enterprises.iwakura.docs.ui.DocumentationViewerPage.PageData;
import lombok.Data;

public class DocumentationViewerPage extends InteractiveCustomUIPage<PageData> {

    public static final String MAIN_CONTENT_SELECTOR = "#Content";

    private final DocumentationViewerService documentationViewerService;
    private DocsContext docsContext;

    public DocumentationViewerPage(
        PlayerRef playerRef,
        DocumentationViewerService documentationViewerService,
        DocsContext docsContext
    ) {
        super(playerRef, CustomPageLifetime.CanDismiss, PageData.CODEC);
        this.documentationViewerService = documentationViewerService;
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
    public void replaceWithContext(DocsContext context, boolean clear) {
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

    @Data
    public static class PageData {

        public static final String OPEN_TOPIC_FIELD = "OpenTopic";

        public static final BuilderCodec<PageData> CODEC = BuilderCodec.builder(PageData.class, PageData::new)
            .append(new KeyedCodec<>(OPEN_TOPIC_FIELD, Codec.STRING), PageData::setOpenTopic, PageData::getOpenTopic)
            .add()
            .build();

        private String openTopic;
    }
}
