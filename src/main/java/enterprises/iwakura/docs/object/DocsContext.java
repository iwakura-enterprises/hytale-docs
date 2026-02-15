package enterprises.iwakura.docs.object;

import java.util.List;

import com.hypixel.hytale.protocol.packets.interface_.CustomUICommand;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBinding;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import enterprises.iwakura.docs.config.DocsConfig;
import enterprises.iwakura.docs.ui.DocumentationViewerPage;
import enterprises.iwakura.docs.util.ReflectionUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
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

    protected List<Documentation> documentations;
    protected Topic topic;

    public static DocsContext of(List<Documentation> documentations, Topic topic) {
        return new DocsContext(
            new UICommandBuilder(),
            new UIEventBuilder(),
            documentations,
            topic
        );
    }

    public static DocsContext of(DocsContext docsContext) {
        return new DocsContext(
            new UICommandBuilder(),
            new UIEventBuilder(),
            docsContext.getDocumentations(),
            docsContext.getTopic()
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

    @Override
    public String toString() {
        return "DocsContext{" +
            "documentations.size=" + documentations.size() +
            ", topic=" + topic +
            '}';
    }
}
