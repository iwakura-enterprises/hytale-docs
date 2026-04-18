package enterprises.iwakura.docs.ui.render;

import java.util.List;
import java.util.Objects;

import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;

import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.docs.service.DocumentationSearchService;
import enterprises.iwakura.docs.service.MarkdownService;
import enterprises.iwakura.docs.ui.CommonStyles;
import enterprises.iwakura.docs.ui.DocumentationViewerPage.PageData;
import enterprises.iwakura.docs.ui.DocumentationViewerPage.PageData.InterfaceAction;
import enterprises.iwakura.docs.ui.render.DocumentationTreeTopicRenderer.RenderData;
import enterprises.iwakura.docs.util.BoyerMooreSearch.SearchPattern;
import enterprises.iwakura.docs.util.InterfaceUtils;
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
    private final MarkdownService markdownService;
    private final DocumentationSearchService documentationSearchService;

    @Override
    public String render(DocsContext ctx, RenderData renderData) {
        var documentation = renderData.getDocumentation();
        var topic = renderData.getTopic();

        var buttonSelector = InterfaceUtils.generateSelector();
        var treeUI = """
            // DocumentationTreeTopicRenderer#render()
            Group {
                LayoutMode: Top;
                Padding: (Left: 15);
            
                {{topic-markup-element}} #{{button-selector}} {
                    Text: "{{name}}";
                    Style: {{button-style}}
                }
            
                {{topics}}
            }
            """;

        StringBuilder topicsUI = new StringBuilder();

        var searchPattern = ctx.hasTopicSearchQuery() ? SearchPattern.of(ctx.getInterfaceState().getTopicSearchQuery()) : null;
        topic.getTopics().stream()
            .filter(childTopic -> !ctx.hasTopicSearchQuery() || documentationSearchService.searchTopic(childTopic, searchPattern, ctx.getInterfaceState().getPreferredLocaleType(), ctx.getInterfaceState().isFullTextSearch()))
            .filter(childTopic -> documentationSearchService.canSeeAnyTopic(ctx.getPlayerRef(), List.of(childTopic)))
            .forEach(childTopic -> {
                topicsUI.append(documentationTreeTopicRenderer.getBeanInstance().render(ctx, new RenderData(documentation, documentationSearchService.getLocalePreferredTopic(childTopic, ctx.getInterfaceState().getPreferredLocaleType()))));
            });

        if (!topic.isCategory()) {
            ctx.getEventBuilder().addEventBinding(
                CustomUIEventBindingType.Activating,
                "#" + buttonSelector,
                new EventData()
                    .append(PageData.INTERFACE_ACTION_FIELD, InterfaceAction.OPEN_TOPIC)
                    .append(PageData.OPEN_TOPIC_FIELD, "%s:%s:%s$%s".formatted(
                        documentation.getGroup(),
                        documentation.getId(),
                        topic.getId(),
                        topic.getLocaleType().getCode()
                    )),
                true
            );
        }

        String buttonStyle;

        if (Objects.equals(ctx.getTopic(), topic)) {
            buttonStyle = topic.isCategory() ? CommonStyles.SELECTED_TOPIC_CATEGORY_STYLE : CommonStyles.SELECTED_TOPIC_BUTTON_STYLE;
        } else if (ctx.hasTopicSearchQuery() && documentationSearchService.searchTopic(topic, searchPattern, ctx.getInterfaceState().getPreferredLocaleType(), ctx.getInterfaceState().isFullTextSearch())) {
            buttonStyle = topic.isCategory() ? CommonStyles.MATCHES_SEARCH_TOPIC_CATEGORY_STYLE : CommonStyles.MATCHES_SEARCH_TOPIC_BUTTON_STYLE;
        } else {
            buttonStyle = topic.isCategory() ? CommonStyles.NORMAL_TOPIC_CATEGORY_STYLE : CommonStyles.NORMAL_TOPIC_BUTTON_STYLE;
        }

        return treeUI
            .replace("{{topic-markup-element}}", topic.isCategory() ? "Label" : "TextButton")
            .replace("{{button-selector}}", buttonSelector)
            .replace("{{button-style}}", buttonStyle)
            .replace("{{name}}", markdownService.escapeText(topic.getName()))
            .replace("{{topics}}", topicsUI);
    }

    @Data
    public static class RenderData {

        private final Documentation documentation;
        private final Topic topic;
    }
}
