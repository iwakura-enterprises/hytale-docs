package enterprises.iwakura.docs.ui.render;

import java.util.Comparator;
import java.util.List;

import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.DocumentationType;
import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class DocumentationTreeRenderer implements Renderer<List<Documentation>> {

    public static final String DOCUMENTATION_TREE_SELECTOR = "#DocumentationTree";

    private final DocumentationRenderer documentationRenderer;

    @Override
    public String render(DocsContext ctx, List<Documentation> documentations) {
        clearAndAppendInline(ctx, documentations);
        return """
            // DocumentationTreeRenderer#render()
            Group {
                Padding: (Vertical: 40);
                Anchor: (Height: 940);
                FlexWeight: 1;
            
                Group {
                    Padding: (Top: 5, Left: 10, Right: 10, Bottom: 10);
                    Background: #121a24;
                    OutlineColor: #203651;
                    OutlineSize: 2;

                    Group {{documentation-tree-selector}} {
                    }
                }
            }
            """.replace("{{documentation-tree-selector}}", DOCUMENTATION_TREE_SELECTOR);
    }

    public void clearAndAppendInline(DocsContext ctx, List<Documentation> documentations) {
        var documentationsUIContext = DocsContext.of(ctx);
        documentationsUIContext.setTopic(ctx.getTopic());
        StringBuilder documentationsUI = new StringBuilder();

        DocumentationType lastType = null;

        for (Documentation documentation : documentations) {
            if (lastType != documentation.getType()) {
                lastType = documentation.getType();

                documentationsUI.append(
                    """
                    Group {
                        Padding: (Bottom: 5);
                    
                        Label {
                            Text: "—— {{type}} docs ——";
                            Style: (
                                TextColor: #4b6b96,
                                HorizontalAlignment: Center
                            );
                        }
                    }
                    """.replace("{{type}}", lastType.getHumanReadable())
                );
            }

            documentationsUI.append(documentationRenderer.render(documentationsUIContext, documentation));
        }

        var ui = """
            // DocumentationTreeRenderer#clearAndAppendInline()
            Group {
                Padding: (Horizontal: 5, Top: 10);
                LayoutMode: TopScrolling;
                ScrollbarStyle: (
                    Spacing: 6,
                    Size: 6,
                    Background: (TexturePath: "Common/Scrollbar.png", Border: 3),
                    Handle: (TexturePath: "Common/ScrollbarHandle.png", Border: 3),
                    HoveredHandle: (TexturePath: "Common/ScrollbarHandleHovered.png", Border: 3),
                    DraggedHandle: (TexturePath: "Common/ScrollbarHandleDragged.png", Border: 3),
                    OnlyVisibleWhenHovered: true
                );

                {{documentations}}
            }
            """.replace("{{documentations}}", documentationsUI);

        ctx.getCommandBuilder().clear(DOCUMENTATION_TREE_SELECTOR);
        ctx.getCommandBuilder().appendInline(DOCUMENTATION_TREE_SELECTOR, ui);
        documentationsUIContext.mergeInto(ctx);
    }
}
