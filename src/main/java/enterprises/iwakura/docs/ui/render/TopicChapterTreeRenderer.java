package enterprises.iwakura.docs.ui.render;

import java.util.ArrayList;
import java.util.List;

import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.node.Text;

import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.docs.service.MarkdownService;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class TopicChapterTreeRenderer implements Renderer<Topic> {

    public static final String TOPIC_CHAPTER_TREE_SELECTOR = "#TopicChapterTree";

    private final MarkdownService markdownService;

    @Override
    public String render(DocsContext ctx, Topic topic) {
        clearAndAppendInline(ctx, topic);
        return """
            // TopicChapterTreeRenderer#render()
            Group {
                Padding: (Vertical: 40);
                FlexWeight: 1;

                Group {
                    Padding: (Top: 5, Left: 10, Right: 10, Bottom: 10);
                    Background: #121a24;
                    OutlineColor: #203651;
                    OutlineSize: 2;

                    Group {{chapters-selector}} {
                    }
                }
            }
            """.replace("{{chapters-selector}}", TOPIC_CHAPTER_TREE_SELECTOR);
    }

    public void clearAndAppendInline(DocsContext ctx, Topic topic) {
        StringBuilder chaptersUI = new StringBuilder(
            """
            // TopicChapterTreeRenderer#clearAndAppendInline()
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
            """);

        var node = markdownService.parseMarkdown(topic.getMarkdownContent(), true);

        if (node != null) {
            for (Heading heading : markdownService.extractHeadings(node)) {
                var padding = 15 * (heading.getLevel() - 1);
                var title = markdownService.extractText(heading);

                chaptersUI.append(
                    """
                    // TopicChapterTreeRenderer#clearAndAppendInline()[heading]
                    Group {
                        LayoutMode: Left;
                        Padding: (Left: {{padding}}, Bottom: 4);

                        TextButton {
                            Text: "{{title}}";
                            Style: (
                                Default: (LabelStyle: (TextColor: #bfbfbf, Wrap: true)),
                                Hovered: (LabelStyle: (TextColor: #ffffffff, Wrap: true)),
                                Pressed: (LabelStyle: (TextColor: #e1e1e1, Wrap: true)),
                                Sounds: (
                                    Activate: (
                                        SoundPath: "Sounds/ButtonsLightActivate.ogg",
                                        MinPitch: -0.4,
                                        MaxPitch: 0.4,
                                        Volume: 4
                                    ),
                                    MouseHover: (
                                        SoundPath: "Sounds/ButtonsLightHover.ogg",
                                        Volume: 6
                                    )
                                )
                            );
                        }
                    }
                    """
                        .replace("{{padding}}", String.valueOf(padding))
                        .replace("{{title}}", title)
                );
            }
        } else {
            chaptersUI.append(
                """
                // TopicChapterTreeRenderer#clearAndAppendInline()[heading#no-chapters]
                Group {
                    LayoutMode: Left;
                    Padding: (Left: {{padding}});

                    TextButton {
                        Text: "No chapters / failed to parse markdown";
                        Style: (
                            Default: (LabelStyle: (TextColor: #bfbfbf, Wrap: true)),
                            Hovered: (LabelStyle: (TextColor: #ffffffff, Wrap: true)),
                            Pressed: (LabelStyle: (TextColor: #e1e1e1, Wrap: true)),
                            Sounds: (
                                Activate: (
                                    SoundPath: "Sounds/ButtonsLightActivate.ogg",
                                    MinPitch: -0.4,
                                    MaxPitch: 0.4,
                                    Volume: 4
                                ),
                                MouseHover: (
                                    SoundPath: "Sounds/ButtonsLightHover.ogg",
                                    Volume: 6
                                )
                            )
                        );
                    }
                }
                """
            );
        }

        chaptersUI.append("}");

        var topicUIContext = DocsContext.of(ctx);
        ctx.getCommandBuilder().clear(TOPIC_CHAPTER_TREE_SELECTOR);
        ctx.getCommandBuilder().appendInline(TOPIC_CHAPTER_TREE_SELECTOR, chaptersUI.toString());
        topicUIContext.mergeInto(ctx);
    }
}
