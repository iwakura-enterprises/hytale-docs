package enterprises.iwakura.docs.ui.render;

import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.docs.ui.render.DocumentationTreeTopicRenderer.RenderData;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class DocumentationRenderer implements Renderer<Documentation> {

    private final DocumentationTreeTopicRenderer documentationTreeTopicRenderer;

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

        documentation.getTopics().forEach(topic -> {
            topicsUI.append(documentationTreeTopicRenderer.render(ctx, new RenderData(documentation, topic)));
        });

        return treeUI
            .replace("{{name}}", documentation.getName())
            .replace("{{topics}}", topicsUI);
    }
}
