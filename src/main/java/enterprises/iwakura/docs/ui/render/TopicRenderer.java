package enterprises.iwakura.docs.ui.render;

import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class TopicRenderer implements Renderer<Topic> {

    public static final String TOPIC_TITLE_SELECTOR = "#TopicTitle";
    public static final String TOPIC_DESCRIPTION_SELECTOR = "#TopicDescription";
    public static final String TOPIC_AUTHOR_SELECTOR = "#TopicAuthor";
    public static final String TOPIC_CONTENT_SELECTOR = "#TopicContent";

    private final TopicContentRenderer topicContentRenderer;

    @Override
    public String render(DocsContext ctx, Topic topic) {
        clearAndAppendInline(ctx, topic);

        return
            """
            // TopicRenderer#render()
            Group {
                FlexWeight: 4;
                LayoutMode: TopScrolling;
                Padding: (Left: 130, Right: 10);
                ScrollbarStyle: (...@DefaultScrollbarStyle, OnlyVisibleWhenHovered: true, Spacing: 130);

                Group {
                    LayoutMode: Left;

                    Label {{topic-title-selector}} {
                        Padding: (Bottom: 5, Top: 40);
                        Text: "{{title}}";
                        Style: (
                            FontSize: 32,
                            Wrap: true,
                            RenderBold: true
                        );
                    }
                }

                Group {
                    LayoutMode: Left;

                    Label {{topic-description-selector}} {
                        Padding: (Bottom: 5);
                        Text: "{{description}}";
                        Style: (
                            FontSize: 24,
                            Wrap: true
                        );
                    }
                }

                Group {
                    LayoutMode: Left;

                    Label {{topic-author-selector}} {
                        Padding: (Bottom: 20);
                        Text: "Written by {{author}}";
                        Style: (
                            FontSize: 14,
                            Wrap: true,
                            TextColor: #d1d3d3
                        );
                    }
                }

                Group #TopicContent {
                    LayoutMode: Top;
                }
            }
            """
                .replace("{{title}}", topic.getName())
                .replace("{{description}}", topic.getDescription())
                .replace("{{author}}", topic.getAuthor())
                .replace("{{topic-content-selector}}", TOPIC_CONTENT_SELECTOR)
                .replace("{{topic-title-selector}}", TOPIC_TITLE_SELECTOR)
                .replace("{{topic-description-selector}}", TOPIC_DESCRIPTION_SELECTOR)
                .replace("{{topic-author-selector}}", TOPIC_AUTHOR_SELECTOR);
    }

    public void clearAndAppendInline(DocsContext ctx, Topic topic) {
        var topicUIContext = DocsContext.of(ctx);
        var topicContentUI = topicContentRenderer.render(topicUIContext, topic);
        ctx.getCommandBuilder().clear(TOPIC_CONTENT_SELECTOR);
        ctx.getCommandBuilder().set(TOPIC_CONTENT_SELECTOR + ".LayoutMode", "Top");
        ctx.getCommandBuilder().set(TOPIC_TITLE_SELECTOR + ".Text", topic.getName());
        ctx.getCommandBuilder().set(TOPIC_DESCRIPTION_SELECTOR + ".Text", topic.getDescription());
        ctx.getCommandBuilder().set(TOPIC_AUTHOR_SELECTOR + ".Text", "Written by " + topic.getAuthor());
        ctx.getCommandBuilder().appendInline(TOPIC_CONTENT_SELECTOR, topicContentUI);
        topicUIContext.mergeInto(ctx);
    }
}
