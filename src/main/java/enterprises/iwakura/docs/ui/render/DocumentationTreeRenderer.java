package enterprises.iwakura.docs.ui.render;

import java.util.List;

import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;

import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.DocumentationType;
import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.docs.service.DocumentationViewerService;
import enterprises.iwakura.docs.ui.DocumentationViewerPage.PageData;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import enterprises.iwakura.sigewine.core.utils.BeanAccessor;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class DocumentationTreeRenderer implements Renderer<List<Documentation>> {

    public static final String DOCUMENTATION_TREE_SELECTOR = "#DocumentationTree";
    public static final String TOPIC_SEARCH_BAR_SELECTOR = "#TopicSearchBar";

    @Bean
    private final BeanAccessor<DocumentationViewerService> documentationViewerService = new BeanAccessor<>(DocumentationViewerService.class);
    private final DocumentationRenderer documentationRenderer;

    @Override
    public String render(DocsContext ctx, List<Documentation> documentations) {
        clearAndAppendInline(ctx, documentations);

        ctx.getEventBuilder().addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            TOPIC_SEARCH_BAR_SELECTOR,
            new EventData().append(PageData.TOPIC_SEARCH_QUERY_FIELD, TOPIC_SEARCH_BAR_SELECTOR + ".Value"),
            false
        );

        var lastTopicQuery = documentationViewerService.getBeanInstance()
            .getInterfacePreferences(ctx.getPlayerRef().getUuid())
            .getLastTopicSearchQuery();

        // Only set if player is not actively searching
        if (!ctx.isSearchActive() && lastTopicQuery != null) {
            ctx.getCommandBuilder().set(TOPIC_SEARCH_BAR_SELECTOR + ".Value", lastTopicQuery);
        }

        return """
            // DocumentationTreeRenderer#render()
            Group {
                Padding: (Vertical: 40);
                LayoutMode: Top;
                FlexWeight: 1;

                Group {
                    Padding: (Left: 10, Right: 10, Bottom: 10);

                    @TextField {{topic-search-bar-selector}} {
                        @Anchor = (Height: 40);
                        PlaceholderText: "Search for topic...";
                    }
                }

                Group {
                    Padding: (Top: 5, Left: 10, Right: 10, Bottom: 10);
                    Anchor: (Height: 810);
                    Background: #121a24;
                    OutlineColor: #203651;
                    OutlineSize: 2;

                    Group {{documentation-tree-selector}} {
                    }
                }
            }
            """
            .replace("{{topic-search-bar-selector}}", TOPIC_SEARCH_BAR_SELECTOR)
            .replace("{{documentation-tree-selector}}", DOCUMENTATION_TREE_SELECTOR);
    }

    public void clearAndAppendInline(DocsContext ctx, List<Documentation> documentations) {
        if (ctx.hasTopicSearchQuery()) {
            documentations = documentations.stream()
                .filter(documentation -> documentation.hasTopicWithName(ctx.getTopicSearchQuery()))
                .toList();
        }

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

        if (documentationsUI.isEmpty()) {
            documentationsUI.append(
                """
                Label {
                    Text: "No documentations found";
                    Style: (
                        TextColor: #4b6b96,
                        HorizontalAlignment: Center
                    );
                }
                """
            );
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
