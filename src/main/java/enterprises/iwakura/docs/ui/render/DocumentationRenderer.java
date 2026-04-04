package enterprises.iwakura.docs.ui.render;

import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.docs.service.MarkdownService;
import enterprises.iwakura.docs.ui.render.DocumentationTreeTopicRenderer.RenderData;
import enterprises.iwakura.docs.util.BoyerMooreSearch.SearchPattern;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class DocumentationRenderer implements Renderer<Documentation> {

    private final DocumentationTreeTopicRenderer documentationTreeTopicRenderer;
    private final MarkdownService markdownService;

    @Override
    public String render(DocsContext ctx, Documentation documentation) {
        var treeUI = """
            // DocumentationRenderer#render()
            Group {
                LayoutMode: Top;
                Padding: (Bottom: 10);

                Label {
                    Text: "{{name}}";
                    Style: (
                        FontSize: 16,
                        FontName: "Secondary",
                        Wrap: true,
                        RenderUppercase: true
                    );
                }

                {{topics}}
            }
            """;

        StringBuilder topicsUI = new StringBuilder();

        var searchPattern = ctx.hasTopicSearchQuery() ? SearchPattern.of(ctx.getInterfaceState().getTopicSearchQuery()) : null;
        documentation.getTopics()
            .stream()
            .filter(childTopic -> !ctx.hasTopicSearchQuery() || childTopic.searchTopic(searchPattern, ctx.getInterfaceState().getPreferredLocaleType(), ctx.getInterfaceState().isFullTextSearch()))
            .forEach(topic -> {
                topicsUI.append(documentationTreeTopicRenderer.render(ctx, new RenderData(documentation, topic.getLocalePreferredTopic(ctx.getInterfaceState().getPreferredLocaleType()))));
            });

        return treeUI
            .replace("{{name}}", markdownService.escapeText(documentation.getName()))
            .replace("{{topics}}", topicsUI);
    }
}
