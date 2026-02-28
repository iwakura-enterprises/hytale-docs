package enterprises.iwakura.docs.object;

import java.util.List;
import java.util.Optional;

import com.hypixel.hytale.common.util.ListUtil;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import enterprises.iwakura.docs.service.FallbackTopicService;
import enterprises.iwakura.docs.ui.DocumentationViewerPage;
import enterprises.iwakura.docs.util.ReflectionUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

/**
 * User Interface context that allows you to merge access the {@link DocumentationViewerPage} and add commands and
 * events that will be added into the resulting command builder and event builder.
 */
@Data
@AllArgsConstructor
public class DocsContext {

    protected final PlayerRef playerRef;
    protected final UICommandBuilder commandBuilder;
    protected final UIEventBuilder eventBuilder;
    protected final InterfaceState interfaceState;

    public static DocsContext of(PlayerRef playerRef, InterfaceState interfaceState) {
        return new DocsContext(
            playerRef,
            new UICommandBuilder(),
            new UIEventBuilder(),
            interfaceState
        );
    }

    public static DocsContext of(DocsContext docsContext) {
        return new DocsContext(
            docsContext.getPlayerRef(),
            new UICommandBuilder(),
            new UIEventBuilder(),
            docsContext.getInterfaceState()
        );
    }

    public void mergeInto(DocsContext context) {
        ReflectionUtils.mergeInto(
            this.getCommandBuilder(), context.getCommandBuilder(),
            this.getEventBuilder(), context.getEventBuilder()
        );
    }

    @SneakyThrows
    public void mergeInto(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder) {
        ReflectionUtils.mergeInto(
            this.getCommandBuilder(), commandBuilder,
            this.getEventBuilder(), eventBuilder
        );
    }

    /**
     * Checks if topic search query is not empty
     *
     * @return True if yes, false otherwise
     */
    public boolean hasTopicSearchQuery() {
        return interfaceState.getTopicSearchQuery() != null && !interfaceState.getTopicSearchQuery().isBlank();
    }

    /**
     * Returns the topic that is currently open in the interface.
     *
     * @return The topic that is currently open in the interface
     */
    public Topic getTopic() {
        return Optional.ofNullable(interfaceState.getTopic()).orElseGet(FallbackTopicService::noTopicSet);
    }

    /**
     * Returns the list of documentations that are currently open in the interface. This is usually the list of
     * documentations that are shown in the left sidebar of the interface.
     *
     * @return The list of documentations that are currently open in the interface
     */
    public List<Documentation> getDocumentations() {
        return interfaceState.getDocumentations();
    }

    @Deprecated
    public String getTopicSearchQuery() {
        return interfaceState.getTopicSearchQuery();
    }

    @Override
    public String toString() {
        return "DocsContext{" +
            "playerRef.uuid=" + playerRef.getUuid() +
            ", state=" + interfaceState +
            '}';
    }
}
