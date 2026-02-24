package enterprises.iwakura.docs.object;

import java.util.List;

import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

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

    protected final UICommandBuilder commandBuilder;
    protected final UIEventBuilder eventBuilder;
    protected final PlayerRef playerRef;

    protected List<Documentation> documentations;
    protected Topic topic;

    protected String topicSearchQuery;
    protected boolean searchActive;

    public static DocsContext of(PlayerRef playerRef, List<Documentation> documentations, Topic topic) {
        return new DocsContext(
            new UICommandBuilder(),
            new UIEventBuilder(),
            playerRef,
            documentations,
            topic,
            "", false
        );
    }

    public static DocsContext of(DocsContext docsContext) {
        return new DocsContext(
            new UICommandBuilder(),
            new UIEventBuilder(),
            docsContext.getPlayerRef(),
            docsContext.getDocumentations(),
            docsContext.getTopic(),
            docsContext.getTopicSearchQuery(), false
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
        return topicSearchQuery != null && !topicSearchQuery.isBlank();
    }

    @Override
    public String toString() {
        return "DocsContext{" +
            "documentations.size=" + documentations.size() +
            ", topic=" + topic +
            ", topicSearchQuery=" + topicSearchQuery +
            '}';
    }
}
