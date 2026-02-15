package enterprises.iwakura.docs.ui.render;

import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.markdown.MarkdownWriter;
import org.springframework.javapoet.CodeBlock;

import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.builder.EventData;

import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.docs.service.MarkdownService;
import enterprises.iwakura.docs.ui.DocumentationViewerPage.PageData;
import enterprises.iwakura.docs.util.ExceptionUtils;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import io.github.insideranh.talemessage.TaleMessage;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class TopicContentRenderer implements Renderer<Topic> {

    public static final String FAILED_TO_PARSE_MARKDOWN =
        """
        &cThere was an exception when parsing markdown content.
        
        &cException: `{{exception-message}}`
        
        &cStacktrace:
        ```
        {{exception-stacktrace}}
        ```
        """;

    public static final String FAILED_TO_WRITE_MARKDOWN =
        """
        Group {
            LayoutMode: Top;
            Background: #D51300;
            Padding: (Full: 10);

            Label {
                Text: "An unrecoverable error has occurred";
                Style: (TextColor: #fdfaff, RenderUppercase: true, RenderBold: true, Wrap: true);
            }

            Label {
                Text: "We're sorry for the inconvenience.";
                Style: (TextColor: #fdfaff, RenderBold: true, Wrap: true);
            }

            Label {
                Padding: (Top: 10);
                Text: "Failed to write parsed markdown content into Hytale UI markup language. See console for more information.";
                Style: (TextColor: #fdfaff, Wrap: true);
            }

            Label {
                Padding: (Top: 10);
                Text: "Topic ID: {{topic-id}}";
                Style: (TextColor: #fdfaff, Wrap: true);
            }

            Label {
                Text: "Exception message: {{exception-message}}";
                Style: (TextColor: #fdfaff, RenderBold: true, Wrap: true);
            }

            Label {
                Padding: (Top: 10);
                Text: "If you're a server administrator, please review the logs and if in doubt, please contact support for Docs mod (mayuna@iwakura.enterprises).";
                Style: (TextColor: #fdfaff, Wrap: true);
            }
        }
        
        Group {
            LayoutMode: Top;
            Padding: (Top: 20);
            Label {Text: "                                                                           "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "                                                                           "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "             +++                                              ++           "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "           ++++++                                           ++++++         "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "          +++++++++                                       ++++++++++       "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "         ++++++++++++                                   +++++++++++++      "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "        +++--++++++++++                               ++++-+-+++++++++     "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "       +++--+++++++++++++                           ++++++++++++++++++     "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "       +++---++++++++++++++                       +++++++++++++++---++     "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "       +++--++++++++++++++++                     +++++++-+++--------++     "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "      ++++-+++--+-++++++++++++                 +++++++++++----------++     "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "      +++++++--+----+++++++++++               ++++++-+-+-----------+++     "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "       +++++++++---------++++++++           +++++++++----------------+     "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "       +++++++++++++--------++++++         +++++++-----------+++++---+     "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "       ++++++++++++++++++++----++++++++++++++++-------++++++++++++++++     "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "       ++++++++++++++++++++++++++++#####+++++---++++++++++++++++-++-+      "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "        ---+--------------#####################+----------+++++++--        "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "        +-------------##############################--------+++++++        "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "        --------++++###################################--------++++        "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "        +----++++########################################+-----+++++       "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "       +-+++++++##########################################+++------+       "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "        ++++++-############################################-++++----+      "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "        +++---##############################################---++++++      "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "         +---################################################--+-++++      "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "        ----##################################################---++        "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "      ++----##################################################----         "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "     ++-----##################################################-----+ +     "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "     +------##################################+###############-----+       "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "            ##############+###################++##############------+      "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "            ###################################++#############+-----+      "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "            ###########+-########################+###########              "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "           ###########+#####################++####+###########             "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "           ##########+########################################             "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "           ##########-#-+--+##############+####+----##########             "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "          ##########+-##++++++-#########+-#+-+--++++###########            "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "          ##########+-++++++++-##########-#+--+-+++#+##########            "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "          ##########+#++++-++++##########-+-+-+-++-#+##########            "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "          ##########+##+++++-+############--+-+-+-##+##########            "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "         #####+#####+#######################---####++##########            "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "        ####++++####+##############################++####+++###            "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "            +++++#################################++###+++++++             "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "             ++++++#--###############################+++++++++             "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "              ++++++++--+#########################+++++++++++              "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "                 +++#++++######++++####+++###+##+##+++++#+++               "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "                 +++++####+###---------###+####-++###++++++                "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "                +++++#####+--+-+-----------------+####++++++               "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "               +#+#######+++--------++-----------+#####++++#+#             "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "               ##########++-------++-+--+++------##########+++             "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "            #########  ##++------++-+++--+-------#############             "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "         #########   ++#+---++--+++++++--------+-++#++###########          "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "      ##########    ++#+++++++++++++++++-+++++------++++#########          "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "     #########   +++++++++++++++++++++++++++++++++++----++########         "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "     #######   +++++++++++++++++++++++++++++++++++++++-----++####          "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "      ####   +++++++++++++++++++++++++++++++++++++++++++------             "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "            ++++++++++++++++++++++++++++++++++++++++++++++++++++           "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "           +++++++++++++++++++++++++++++++++++########+++++++######        "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "         +++++#++++##########+++++++++++++++############++###########      "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "      #########################+##++++++++##########################       "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "     ###############################++++############################       "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "     ##############################################################        "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "     ########+###########################################  #######+        "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "     ########+###########################################++  ######        "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "        ##   #############+++#+#+######+-#+##############+    ###          "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "             #############+++++++###+#+#+##+ ############                  "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "              ###########++++ +       +#    ++###########                  "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "               #########+                      ########-                   "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
            Label {Text: "                 +++++                            ++++                     "; Style: (TextColor: #5c87eb(0.1), FontSize: 4, FontName: "Mono", HorizontalAlignment: Center);}
        }
        """;

    private final MarkdownService markdownService;
    private final Logger logger;

    @Override
    public String render(DocsContext ctx, Topic topic) {
        DocsContext topicContentDocsContext = DocsContext.of(ctx);

        Node document;
        try {
            document = markdownService.parseMarkdown(topic.getMarkdownContent(), false);
        } catch (Exception exception) {
            logger.error("Failed to parse markdown for topic content renderer! Topic ID: %s".formatted(topic.getId()),
                exception);
            document = markdownService.parseMarkdown(FAILED_TO_PARSE_MARKDOWN
                    .replace("{{exception-message}}", exception.getMessage())
                    .replace("{{exception-stacktrace}}", ExceptionUtils.dumpExceptionStacktrace(null, exception)),
                true);
        }

        try {
            if (document == null) {
                throw new IllegalStateException("Failed to parse markdown content which resulted in markdown error message which then wasn't properly parsed! This is a bug in Docs.");
            }

            var output = new StringBuilder();
            MarkdownWriter writer = new MarkdownWriter(output);
            HytaleUIRenderer renderer = new HytaleUIRenderer(writer, topicContentDocsContext);

            document.accept(renderer);
            // Prevent getting hytale UI Renderer's command builders/event builders into the main one
            // if error occurs
            topicContentDocsContext.mergeInto(ctx);
            return output.toString();
        } catch (Exception exception) {
            logger.error("Failed to write parsed markdown content into Hytale UI markup language!", exception);
            return FAILED_TO_WRITE_MARKDOWN
                .replace("{{topic-id}}", topic.getId())
                .replace("{{exception-message}}", exception.getMessage());
        }
    }

    @RequiredArgsConstructor
    public class HytaleUIRenderer extends AbstractVisitor implements NodeRenderer {

        private final MarkdownWriter writer;
        private final DocsContext docsContext;

        private ListHolder listHolder;
        private Message message;

        @Override
        public Set<Class<? extends Node>> getNodeTypes() {
            return Set.of(
                BlockQuote.class,
                BulletList.class,
                Code.class,
                Document.class,
                Emphasis.class,
                FencedCodeBlock.class,
                HardLineBreak.class,
                Heading.class,
                HtmlBlock.class,
                HtmlInline.class,
                Image.class,
                IndentedCodeBlock.class,
                Link.class,
                ListItem.class,
                OrderedList.class,
                Paragraph.class,
                SoftLineBreak.class,
                StrongEmphasis.class,
                Text.class,
                ThematicBreak.class
            );
        }

        @Override
        public void render(Node node) {
            node.accept(this);
        }

        @Override
        public void visit(Document document) {
            // No rendering itself
            visitChildren(document);
            writer.line();
        }

        @Override
        public void visit(Heading heading) {
            var fontSize = switch (heading.getLevel()) {
                case 1 -> 30;
                case 2 -> 26; // 4
                case 3 -> 22; // 4
                case 4 -> 18; // 4
                case 5 -> 14;
                case 6 -> 10;
                default -> 16;
            };
            writer.line();

            var breakUI =
                """
                Group {
                    Padding: (Bottom: 16);

                    Group {
                        Anchor: (Height: {{height}});
                        Background: #2f4f77;
                    }
                }
                """.replace("{{height}}", String.valueOf(heading.getLevel() == 1 ? 4 : 2));

            writer.raw(
                """
                    // TopicContentRender#visit(Heading)
                    Group {
                        LayoutMode: Top;

                        Label {
                            Text: "{{title}}";
                            Style: (
                                FontSize: {{font-size}},
                                RenderBold: true,
                                Wrap: true
                            );
                        }
                        
                        {{break}}
                    }
                    """
                    .replace("{{title}}", markdownService.escapeText(markdownService.extractText(heading)))
                    .replace("{{font-size}}", String.valueOf(fontSize))
                    .replace("{{break}}", heading.getLevel() <= 2 ? breakUI : "")
            );
            writer.block();
        }

        @Override
        public void visit(Paragraph paragraph) {
            var textSelector = generateTextSelector();
            // Skips padding at the bottom if it is in list and is not the last item
            boolean shouldSkipPadding =
                paragraph.getNext() instanceof BlockQuote ||
                paragraph.getNext() instanceof FencedCodeBlock ||
                paragraph.getNext() instanceof IndentedCodeBlock ||
                paragraph.getParent() instanceof ListItem ||
                paragraph.getParent() instanceof BlockQuote ||
                (paragraph.getParent() instanceof Document &&
                    paragraph.getPrevious() == null && paragraph.getNext() == null);

            writer.line();
            writer.raw(
                """
                    // TopicContentRender#visit(Paragraph)
                    Group {
                        Padding: (Bottom: {{bottom-padding}});
                        LayoutMode: Left;

                        Label #{{label-selector}} {
                            Style: (Wrap: true);
                        }
                    }
                    """
                    .replace("{{label-selector}}", textSelector)
                    .replace("{{bottom-padding}}", shouldSkipPadding ? "0" : "8")
            );

            // Prepare message for any text
            message = Message.raw("");
            visitChildren(paragraph);
            docsContext.getCommandBuilder().set("#" + textSelector + ".TextSpans", message);

            writer.block();
        }

        @Override
        public void visit(Text text) {
            if (message != null) {
                var previousHtmlTags = markdownService.extractHtmlTags(text, false);
                var nextHtmlTags = markdownService.extractHtmlTags(text, true);

                // Clear up HTML tags if both are not populated
                if (previousHtmlTags.isEmpty()) {
                    nextHtmlTags = "";
                }
                if (nextHtmlTags.isEmpty()) {
                    previousHtmlTags = "";
                }

                if (!previousHtmlTags.isEmpty()) {
                    // Transform <tag data="a:b:c"> to <tag:a:b:c>
                    previousHtmlTags = previousHtmlTags.replaceAll("<(\\w+)\\s+data=\"([^\"]*)\">", "<$1:$2>");
                }

                message.insert(TaleMessage.parse(previousHtmlTags + text.getLiteral() + nextHtmlTags));
            }
        }

        @Override
        public void visit(Emphasis emphasis) {
            var shouldBeBold = emphasis.getFirstChild() instanceof StrongEmphasis;

            if (message != null) {
                message.insert(TaleMessage.parse(markdownService.escapeText(markdownService.extractText(emphasis))).italic(true).bold(shouldBeBold));
            }
        }

        @Override
        public void visit(StrongEmphasis strongEmphasis) {
            if (message != null) {
                message.insert(TaleMessage.parse(markdownService.escapeText(markdownService.extractText(strongEmphasis))).bold(true));
            }
        }

        @Override
        public void visit(BlockQuote blockQuote) {
            boolean shouldSkipTopPadding =
                blockQuote.getPrevious() instanceof BlockQuote ||
                    blockQuote.getPrevious() instanceof Heading;

            writer.line();
            writer.raw(
                """
                    // TopicContentRender#visit(BlockQuote)
                    Group {
                        Padding: (Horizontal: 15, Bottom: 16, Top: {{top-padding}});
                        LayoutMode: Left;

                        Group {
                            LayoutMode: Top;
                            Padding: (Full: 15);
                            Background: #121a24;
                            OutlineColor: #203651;
                            OutlineSize: 2;
                    """.replace("{{top-padding}}", shouldSkipTopPadding ? "0" : "16")
            );

            visitChildren(blockQuote);

            writer.raw("}}");
            writer.block();
        }

        @Override
        public void visit(Code code) {
            if (message != null) {
                message.insert(Message.raw(code.getLiteral()).monospace(true).color("#cccccc"));
            }
        }

        @Override
        public void visit(FencedCodeBlock fencedCodeBlock) {
            var skipBottomPadding = fencedCodeBlock.getNext() == null;
            var textSelector = generateTextSelector();
            var literal = fencedCodeBlock.getLiteral();

            // Trim any blank lines at the bottom
            literal = literal.replaceAll("(\r?\n)+$", "");

            writer.line();

            writer.raw(
                """
                    // TopicContentRender#visit(FencedCodeBlock)
                    Group {
                        Padding: (Top: 8, Bottom: {{bottom-padding}});
                        LayoutMode: Left;

                        Group {
                            Padding: (Full: 15);
                            Background: #121a24;
                            OutlineColor: #203651;
                            OutlineSize: 2;

                            Label #{{label-selector}} {
                                Style: (Wrap: true, FontName: "Mono", TextColor: #cccccc);
                            }
                        }
                    }
                    """
                    .replace("{{label-selector}}", textSelector)
                    .replace("{{bottom-padding}}", skipBottomPadding ? "0" : "8")
            );

            writer.block();

            docsContext.getCommandBuilder().set("#" + textSelector + ".Text", literal);
        }

        @Override
        public void visit(IndentedCodeBlock indentedCodeBlock) {
            var skipBottomPadding = indentedCodeBlock.getNext() == null;
            var textSelector = generateTextSelector();
            var literal = indentedCodeBlock.getLiteral();

            // Trim any blank lines at the bottom
            literal = literal.replaceAll("(\r?\n)+$", "");

            writer.line();

            writer.raw(
                """
                    // TopicContentRender#visit(IndentedCodeBlock)
                    Group {
                        Padding: (Top: 8, Bottom: {{bottom-padding}});
                        LayoutMode: Left;

                        Group {
                            Padding: (Full: 15);
                            Background: #0e151d;
                            OutlineColor: #203651;
                            OutlineSize: 2;

                            Label #{{label-selector}} {
                                Style: (Wrap: true, FontName: "Mono", TextColor: #cccccc);
                            }
                        }
                    }
                    """
                    .replace("{{label-selector}}", textSelector)
                    .replace("{{bottom-padding}}", skipBottomPadding ? "0" : "8")
            );

            writer.block();

            docsContext.getCommandBuilder().set("#" + textSelector + ".Text", literal);
        }

        @Override
        public void visit(HtmlInline htmlInline) {
            // Skip - added support in Text
        }

        @Override
        public void visit(HtmlBlock htmlBlock) {
            /*
            <buttons>
                <button topic="IwakuraEnterprises:another_topic_child1">Click me to get to second child!</button>
                <button topic="IwakuraEnterprises:another_topic_child1">Go to second child</button>
                <button topic="IwakuraEnterprises:another_topic_child1">Second child</button>
            </buttons>
             */
            writer.raw(
                """
                // TopicContentRender#visit(HtmlBlock)
                Group {
                    LayoutMode: LeftCenterWrap;
                """);

            HtmlBlockParser.parse(markdownService, htmlBlock.getLiteral(), writer, docsContext);

            writer.raw("}");
        }

        @Override
        public void visit(BulletList bulletList) {
            boolean isTopLevelList = listHolder == null;

            writer.line();
            writer.raw(
                """
                    // TopicContentRender#visit(BulletList)
                    Group {
                        Padding: (Left: 15, Bottom: {{bottom-padding}});
                        LayoutMode: Top;
                    """.replace("{{bottom-padding}}", isTopLevelList ? "10" : "0")
            );
            listHolder = new BulletListHolder(listHolder, bulletList);
            visitChildren(bulletList);
            listHolder = listHolder.parent;
            writer.raw("}");
            writer.block();
        }

        @Override
        public void visit(OrderedList orderedList) {
            boolean isTopLevelList = listHolder == null;

            writer.line();
            writer.raw(
                """
                    // TopicContentRender#visit(OrderedList)
                    Group {
                        Padding: (Left: 15, Bottom: {{bottom-padding}});
                        LayoutMode: Top;
                    """.replace("{{bottom-padding}}", isTopLevelList ? "10" : "0")
            );
            listHolder = new OrderedListHolder(listHolder, orderedList);
            visitChildren(orderedList);
            listHolder = listHolder.parent;
            writer.raw("}");
            writer.block();
        }

        @Override
        public void visit(ListItem listItem) {
            var markerSelector = generateTextSelector();
            var textSelector = generateTextSelector();

            String marker;
            if (listHolder instanceof BulletListHolder bulletListHolder) {
                marker = listItem.getMarkerIndent() == 0 ? "•" : "o";
            } else if (listHolder instanceof OrderedListHolder orderedListHolder) {
                marker = (listItem.getMarkerIndent() == 0 ? String.valueOf(orderedListHolder.number)
                    : orderedListHolder.getNumberAsRomanNumeral()) + ".";
                orderedListHolder.number++;
            } else {
                throw new IllegalStateException("Unknown list holder type: " + listHolder);
            }

            writer.raw(
                """
                    // TopicContentRender#visit(ListItem)
                    Group {
                        LayoutMode: Top;
                    
                        Group {
                            LayoutMode: Left;
                            Padding: (Bottom: 4);
                    
                            Group {
                                Padding: (Right: 2);
                                Label #{{marker-selector}} {
                                    Style: (Wrap: true);
                                }
                            }

                            Group {
                                Label #{{label-selector}} {
                                    Style: (Wrap: true);
                                }
                            }
                        }
                    """
                    .replace("{{marker-selector}}", markerSelector)
                    .replace("{{label-selector}}", textSelector)
            );

            message = Message.raw(" ");
            Node child = listItem.getFirstChild();
            if (child instanceof Paragraph) {
                visitChildren(child);
                child = child.getNext();
            }

            docsContext.getCommandBuilder().set("#" + markerSelector + ".TextSpans", Message.raw(marker));
            docsContext.getCommandBuilder().set("#" + textSelector + ".TextSpans", message);

            while (child != null) {
                child.accept(this);
                child = child.getNext();
            }

            writer.raw("}");
            writer.block();
        }

        @Override
        public void visit(SoftLineBreak softLineBreak) {
            if (message != null) {
                message = message.insert(" ");
            }
        }

        @Override
        public void visit(ThematicBreak thematicBreak) {
            writer.line();

            writer.raw(
                """
                    // TopicContentRender#visit(ThematicBreak)
                    Group {
                        Padding: (Top: 8, Bottom: 16, Horizontal: 300);

                        Group {
                            Anchor: (Height: 3);
                            Background: #2f4f77;
                        }
                    }
                    """
            );

            writer.block();
        }

        /**
         * Generates selector for text
         *
         * @return Selector (without hashtag)
         */
        private String generateTextSelector() {
            return "GeneratedText" + UUID.randomUUID().toString().replace("-", "");
        }

        //#region Helper classes...

        @Data
        private static class ListHolder {

            final ListHolder parent;
            private boolean shouldHaveInnerPoints;

            protected ListHolder(ListHolder parent) {
                this.parent = parent;
            }
        }

        private static class BulletListHolder extends ListHolder {


            public BulletListHolder(ListHolder parent, BulletList bulletList) {
                super(parent);
            }
        }

        private static class OrderedListHolder extends ListHolder {

            private int number;

            protected OrderedListHolder(ListHolder parent, OrderedList orderedList) {
                super(parent);
                number = orderedList.getMarkerStartNumber() != null ? orderedList.getMarkerStartNumber() : 1;
            }

            public String getNumberAsRomanNumeral() {
                int num = this.number;
                if (num <= 0 || num > 3999) {
                    return String.valueOf(num);
                }

                String[] thousands = {"", "m", "mm", "mmm"};
                String[] hundreds = {"", "c", "cc", "ccc", "cd", "d", "dc", "dcc", "dccc", "cm"};
                String[] tens = {"", "x", "xx", "xxx", "xl", "l", "lx", "lxx", "lxxx", "xc"};
                String[] ones = {"", "i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix"};

                return thousands[num / 1000] + hundreds[(num % 1000) / 100] + tens[(num % 100) / 10] + ones[num % 10];
            }
        }

        @RequiredArgsConstructor
        private static class HtmlBlockParser {

            private static final Pattern WRAPPER_PATTERN = Pattern.compile("<(\\w+)>([\\s\\S]*?)</\\1>");
            private static final Pattern INNER_PATTERN = Pattern.compile("<(\\w+)\\s+(\\w+)=\"([^\"]+)\">([^<]*)</\\1>");

            public static void parse(MarkdownService markdownService, String htmlBlock, MarkdownWriter writer, DocsContext docsContext) {
                var wrapperMatcher = WRAPPER_PATTERN.matcher(htmlBlock);

                if (wrapperMatcher.find()) {
                    var wrapperName = wrapperMatcher.group(1);
                    var innerContent = wrapperMatcher.group(2);

                    if (wrapperName.equals("buttons")) {
                        var innerMatcher = INNER_PATTERN.matcher(innerContent);

                        while (innerMatcher.find()) {
                            var buttonSelector = generateButtonSelector();
                            var elementName = innerMatcher.group(1);    // "button"
                            var attributeName = innerMatcher.group(2);  // "topic"
                            var attributeValue = innerMatcher.group(3); // "IwakuraEnterprises:another_topic_child1"
                            var textContent = innerMatcher.group(4);    // "Click me to get to second child!"

                            if (elementName.equals("button")) {
                                writer.raw(
                                    """
                                        // TopicContentRender.HtmlBlockParser#parse()[button]
                                        Group {
                                            Padding: (Right: 10, Bottom: 8);
                                        
                                            TextButton #{{button-selector}} {
                                                Text: "{{button-content}}";
                                                Style: (
                                                    Default: (Background: PatchStyle(TexturePath: "Common/Buttons/Secondary.png", Border: 12), LabelStyle: (FontSize: 17, RenderBold: true, RenderUppercase: true, HorizontalAlignment: Center, VerticalAlignment: Center, TextColor: #bdcbd3)),
                                                    Hovered: (Background: PatchStyle(TexturePath: "Common/Buttons/Secondary_Hovered.png", Border: 12), LabelStyle: (FontSize: 17, RenderBold: true, RenderUppercase: true, HorizontalAlignment: Center, VerticalAlignment: Center, TextColor: #bdcbd3)),
                                                    Pressed: (Background: PatchStyle(TexturePath: "Common/Buttons/Secondary_Pressed.png", Border: 12), LabelStyle: (FontSize: 17, RenderBold: true, RenderUppercase: true, HorizontalAlignment: Center, VerticalAlignment: Center, TextColor: #bdcbd3)),
                                                    Disabled: (Background: PatchStyle(TexturePath: "Common/Buttons/Disabled.png", Border: 12), LabelStyle: (FontSize: 17, RenderBold: true, RenderUppercase: true, HorizontalAlignment: Center, VerticalAlignment: Center, TextColor: #bdcbd3)),
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
                                                Anchor: (Height: 44);
                                                Padding: (Horizontal: 24);
                                            }
                                        }
                                        """
                                            .replace("{{button-content}}", markdownService.escapeText(textContent))
                                            .replace("{{button-selector}}", buttonSelector)
                                );

                                if (attributeName.equals("topic")) {
                                    docsContext.getEventBuilder().addEventBinding(
                                        CustomUIEventBindingType.Activating,
                                        "#" + buttonSelector,
                                        new EventData().append(PageData.OPEN_TOPIC_FIELD, attributeValue),
                                        false
                                    );
                                }
                            }
                        }
                    }
                }
            }

            /**
             * Generates selector for button
             *
             * @return Selector (without hashtag)
             */
            private static String generateButtonSelector() {
                return "GeneratedButton" + UUID.randomUUID().toString().replace("-", "");
            }
        }

        //#endregion
    }
}
