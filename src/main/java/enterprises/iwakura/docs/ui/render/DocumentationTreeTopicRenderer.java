package enterprises.iwakura.docs.ui.render;

import java.util.Objects;
import java.util.UUID;

import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;

import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.docs.ui.CommandStyles;
import enterprises.iwakura.docs.ui.DocumentationViewerPage.PageData;
import enterprises.iwakura.docs.ui.render.DocumentationTreeTopicRenderer.RenderData;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import enterprises.iwakura.sigewine.core.utils.BeanAccessor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class DocumentationTreeTopicRenderer implements Renderer<RenderData> {

    @Bean
    private final BeanAccessor<DocumentationTreeTopicRenderer> documentationTreeTopicRenderer =
        new BeanAccessor<>(DocumentationTreeTopicRenderer.class);

    @Override
    public String render(DocsContext ctx, RenderData renderData) {
        var documentation = renderData.getDocumentation();
        var topic = renderData.getTopic();

        var buttonSelector = generateButtonSelector();
        var treeUI = """
            // DocumentationTreeTopicRenderer#render()
            Group {
                LayoutMode: Top;
                Padding: (Left: 15);
            
                TextButton #{{button-selector}} {
                    Text: "{{name}}";
                    Style: {{button-style}}
                }
            
                {{topics}}
            }
            """;

        StringBuilder topicsUI = new StringBuilder();

        topic.getTopics().forEach(childTopic -> {
            topicsUI.append(documentationTreeTopicRenderer.getBeanInstance().render(ctx, new RenderData(documentation, childTopic)));
        });

        ctx.getEventBuilder().addEventBinding(
            CustomUIEventBindingType.Activating,
            "#" + buttonSelector,
            new EventData()
                .append(PageData.OPEN_TOPIC_FIELD, "%s:%s:%s".formatted(
                    documentation.getGroup(),
                    documentation.getId(),
                    topic.getId()
                )),
            false
        );

        return treeUI
            .replace("{{button-selector}}", buttonSelector)
            .replace("{{button-style}}", Objects.equals(ctx.getTopic(), topic) ? CommandStyles.SELECTED_BUTTON_STYLE : CommandStyles.NORMAL_BUTTON_STYLE)
            .replace("{{name}}", topic.getName())
            .replace("{{topics}}", topicsUI);
    }

    /**
     * Generates selector for button
     *
     * @return Selector (without hashtag)
     */
    private String generateButtonSelector() {
        return "GeneratedButton" + UUID.randomUUID().toString().replace("-", "");
    }

    @Data
    public static class RenderData {

        private final Documentation documentation;
        private final Topic topic;
    }
}
