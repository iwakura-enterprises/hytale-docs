package enterprises.iwakura.docs.ui.render;

import java.util.List;

import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.builder.EventData;

import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.DocumentationType;
import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.docs.service.DocumentationService;
import enterprises.iwakura.docs.service.DocumentationViewerService;
import enterprises.iwakura.docs.ui.CommonStyles;
import enterprises.iwakura.docs.ui.DocumentationViewerPage.PageData;
import enterprises.iwakura.docs.ui.DocumentationViewerPage.PageData.InterfaceAction;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import enterprises.iwakura.sigewine.core.utils.BeanAccessor;
import io.github.insideranh.talemessage.TaleMessage;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class DocumentationTreeRenderer implements Renderer<List<Documentation>> {

    public static final int MAX_HISTORY = 20;
    public static final String DOCUMENTATION_TREE_SELECTOR = "#DocumentationTree";
    public static final String GO_BACK_BUTTON_SELECTOR = "#GoBackButton";
    public static final String GO_FORWARD_BUTTON_SELECTOR = "#GoForwardButton";
    public static final String GO_HOME_BUTTON_SELECTOR = "#GoHomeButton";
    public static final String TOPIC_SEARCH_BAR_SELECTOR = "#TopicSearchBar";

    @Bean
    private final BeanAccessor<DocumentationViewerService> documentationViewerService = new BeanAccessor<>(DocumentationViewerService.class);
    private final DocumentationRenderer documentationRenderer;
    private final DocumentationService documentationService;

    @Override
    public String render(DocsContext ctx, List<Documentation> documentations) {
        clearAndAppendInline(ctx, documentations);

        // Search
        ctx.getEventBuilder().addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            TOPIC_SEARCH_BAR_SELECTOR,
            new EventData()
                .append(PageData.INTERFACE_ACTION_FIELD, InterfaceAction.SEARCH)
                .append(PageData.TOPIC_SEARCH_QUERY_FIELD, TOPIC_SEARCH_BAR_SELECTOR + ".Value"),
            false
        );

        var lastTopicQuery = documentationViewerService.getBeanInstance()
            .getInterfacePreferences(ctx.getPlayerRef().getUuid())
            .getLastTopicSearchQuery();

        if (lastTopicQuery != null) {
            ctx.getCommandBuilder().set(TOPIC_SEARCH_BAR_SELECTOR + ".Value", lastTopicQuery);
        }

        // Go back / go forward / home buttons

        ctx.getEventBuilder().addEventBinding(
            CustomUIEventBindingType.Activating,
            GO_BACK_BUTTON_SELECTOR,
            new EventData().append(PageData.INTERFACE_ACTION_FIELD, InterfaceAction.BACK.name()),
            false
        );

        ctx.getEventBuilder().addEventBinding(
            CustomUIEventBindingType.Activating,
            GO_FORWARD_BUTTON_SELECTOR,
            new EventData().append(PageData.INTERFACE_ACTION_FIELD, InterfaceAction.FORWARD.name()),
            false
        );

        ctx.getEventBuilder().addEventBinding(
            CustomUIEventBindingType.Activating,
            GO_HOME_BUTTON_SELECTOR,
            new EventData().append(PageData.INTERFACE_ACTION_FIELD, InterfaceAction.HOME.name()),
            false
        );

        return """
            // DocumentationTreeRenderer#render()
            Group {
                Padding: (Vertical: 40);
                LayoutMode: Top;
                FlexWeight: 1;

                Group {
                    LayoutMode: Left;
                    Padding: (Left: 10, Right: 10, Bottom: 10);

                    Group {
                        Padding: (Right: 10);

                        TextButton {{go-back-button-selector}} {
                            Anchor: (Width: 40);
                            Style: {{interface-button-style}};
                            Disabled: {{go-back-button-disabled}};
                            TextTooltipStyle: {{interface-button-tooltip-style}};
                            TextTooltipShowDelay: 0.1;
                        }

                        AssetImage {
                            Anchor: (Width: 24, Height: 24);
                            AssetPath: "UI/Custom/Docs/Images/left-arrow.png";
                        }
                    }

                    Group {
                        Padding: (Right: 10);

                        TextButton {{go-forward-button-selector}} {
                            Anchor: (Width: 40);
                            Style: {{interface-button-style}};
                            Disabled: {{go-forward-button-disabled}};
                            TextTooltipStyle: {{interface-button-tooltip-style}};
                            TextTooltipShowDelay: 0.1;
                        }

                        AssetImage {
                            Anchor: (Width: 24, Height: 24);
                            AssetPath: "UI/Custom/Docs/Images/right-arrow.png";
                        }
                    }

                    Group {
                        Padding: (Right: 10);

                        TextButton {{go-home-button-selector}} {
                            Anchor: (Width: 40);
                            Style: {{interface-button-style}};
                            TooltipText: "Opens the default topic";
                            TextTooltipStyle: {{interface-button-tooltip-style}};
                        }

                        AssetImage {
                            Anchor: (Width: 24, Height: 24);
                            AssetPath: "UI/Custom/Docs/Images/home.png";
                        }
                    }

                    @TextField {{topic-search-bar-selector}} {
                        @Anchor = (Height: 40, Width: 145);
                        PlaceholderText: "Search for topic...";
                    }
                }

                Group {
                    Padding: (Top: 5, Left: 10, Right: 10, Bottom: 10);
                    Anchor: (Height: 810);
                    Background: #121a24;
                    OutlineColor: #203651;
                    OutlineSize: 2;

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

                        Group {{documentation-tree-selector}} {

                        }
                    }
                }
            }
            """
            .replace("{{go-back-button-selector}}", GO_BACK_BUTTON_SELECTOR)
            .replace("{{go-forward-button-selector}}", GO_FORWARD_BUTTON_SELECTOR)
            .replace("{{go-home-button-selector}}", GO_HOME_BUTTON_SELECTOR)
            .replace("{{go-back-button-disabled}}", String.valueOf(!ctx.getInterfaceState().canGoBack()))
            .replace("{{go-forward-button-disabled}}", String.valueOf(!ctx.getInterfaceState().canGoForward()))
            .replace("{{topic-search-bar-selector}}", TOPIC_SEARCH_BAR_SELECTOR)
            .replace("{{documentation-tree-selector}}", DOCUMENTATION_TREE_SELECTOR)
            .replaceAll("\\{\\{interface-button-style}}", CommonStyles.INTERFACE_BUTTON_STYLE)
            .replaceAll("\\{\\{interface-button-tooltip-style}}", CommonStyles.TOOLTIP_STYLE);
    }

    public void clearAndAppendInline(DocsContext ctx, List<Documentation> documentations) {
        if (ctx.hasTopicSearchQuery()) {
            documentations = documentations.stream()
                .filter(documentation -> documentation.hasTopicWithName(ctx.getTopicSearchQuery()))
                .toList();
        }

        var documentationsUIContext = DocsContext.of(ctx);
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

        var topicHistoryMessage = createTopicHistoryMessage(ctx);

        ctx.getCommandBuilder().clear(DOCUMENTATION_TREE_SELECTOR);
        ctx.getCommandBuilder().appendInline(DOCUMENTATION_TREE_SELECTOR, ui);
        ctx.getCommandBuilder().set(GO_BACK_BUTTON_SELECTOR + ".Disabled", !ctx.getInterfaceState().canGoBack());
        ctx.getCommandBuilder().set(GO_FORWARD_BUTTON_SELECTOR + ".Disabled", !ctx.getInterfaceState().canGoForward());
        ctx.getCommandBuilder().set(GO_BACK_BUTTON_SELECTOR + ".TooltipTextSpans", topicHistoryMessage);
        ctx.getCommandBuilder().set(GO_FORWARD_BUTTON_SELECTOR + ".TooltipTextSpans", topicHistoryMessage);
        documentationsUIContext.mergeInto(ctx);
    }

    private Message createTopicHistoryMessage(DocsContext ctx) {
        var interfaceState = ctx.getInterfaceState();
        if (interfaceState.hasHistory()) {
            StringBuilder messageBuilder = new StringBuilder();
            var history = interfaceState.getTopicIdentifierHistory();
            int currentIndex = interfaceState.getTopicIdentifierHistoryIndex();
            int lastIndex = history.size() - 1;

            // Paginate from the end: page 0 = [lastIndex - MAX_HISTORY + 1 .. lastIndex]
            // page 1 = [lastIndex - 2*MAX_HISTORY + 1 .. lastIndex - MAX_HISTORY], ...
            int distanceFromEnd = lastIndex - currentIndex;
            int page = distanceFromEnd / MAX_HISTORY;

            int windowEnd = lastIndex - (page * MAX_HISTORY);
            int windowStart = Math.max(windowEnd - MAX_HISTORY + 1, 0);

            boolean hasMoreAfter = windowEnd < lastIndex;
            boolean hasMoreBefore = windowStart > 0;

            if (hasMoreAfter) {
                messageBuilder.append("(...)\n");
            }

            for (int i = windowEnd; i >= windowStart; i--) {
                var optionalTopic = documentationService.findTopic(ctx.getDocumentations(), history.get(i), null);
                if (optionalTopic.isPresent()) {
                    var topic = optionalTopic.get();
                    boolean shouldBeBold = i == currentIndex;

                    if (shouldBeBold) {
                        messageBuilder.append("<bold> > ");
                    }

                    messageBuilder.append("%s <gray>(%s)</gray>".formatted(
                        topic.getName(), topic.getDocumentation().getName()
                    ));

                    if (shouldBeBold) {
                        messageBuilder.append("</bold>");
                    }

                    if (i != windowStart) {
                        messageBuilder.append("\n");
                    }
                }
            }

            if (hasMoreBefore) {
                messageBuilder.append("\n(...)");
            }

            return TaleMessage.parse(messageBuilder.toString());
        } else {
            return TaleMessage.parse("<gray>Empty topic history</gray>");
        }
    }
}
