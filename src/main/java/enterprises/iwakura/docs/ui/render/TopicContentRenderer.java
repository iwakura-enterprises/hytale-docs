package enterprises.iwakura.docs.ui.render;

import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableBody;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableCell.Alignment;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.ext.gfm.tables.TableRow;
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
import org.commonmark.node.ListBlock;
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

import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.builder.EventData;

import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.docs.service.MarkdownService;
import enterprises.iwakura.docs.service.RuntimeImageAssetService;
import enterprises.iwakura.docs.ui.CommonStyles;
import enterprises.iwakura.docs.ui.DocumentationViewerPage.PageData;
import enterprises.iwakura.docs.ui.DocumentationViewerPage.PageData.InterfaceAction;
import enterprises.iwakura.docs.util.ExceptionUtils;
import enterprises.iwakura.docs.util.InterfaceUtils;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.docs.util.ResizeUtils;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import fr.brouillard.oss.commonmark.ext.notifications.NotificationBlock;
import io.github.insideranh.talemessage.TaleMessage;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class TopicContentRenderer implements Renderer<Topic> {

    public static final Pattern IMAGE_RESIZE_HINT_PATTERN = Pattern.compile("\\{(\\d+)x(\\d+)}$");
    public static final int MAX_IMAGE_SIZE = 900;

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
    private final RuntimeImageAssetService runtimeImageAssetService;
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
                .replace("{{exception-message}}", markdownService.escapeText(exception.getMessage()));
        }
    }

    @RequiredArgsConstructor
    public class HytaleUIRenderer extends AbstractVisitor implements NodeRenderer {

        private final MarkdownWriter writer;
        private final DocsContext docsContext;

        private ListHolder listHolder;
        private Message message;
        private boolean tableRenderingHead;

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
                ThematicBreak.class,
                TableBlock.class,
                TableHead.class,
                TableBody.class,
                TableRow.class,
                TableCell.class,
                NotificationBlock.class
            );
        }

        @Override
        public void visit(Document document) {
            // No rendering itself
            visitChildren(document);
            writer.line();
        }

        @Override
        protected void visitChildren(Node parent) {
            Node child = parent.getFirstChild();
            while (child != null) {
                Node next = child.getNext();
                render(child);
                child = next;
            }
        }

        @Override
        public void render(Node node) {
            if (node instanceof TableBlock) {
                visit((TableBlock) node);
            } else if (node instanceof TableHead) {
                visit((TableHead) node);
            } else if (node instanceof TableBody) {
                visit((TableBody) node);
            } else if (node instanceof TableRow) {
                visit((TableRow) node);
            } else if (node instanceof TableCell) {
                visit((TableCell) node);
            } else if (node instanceof NotificationBlock) {
                visit((NotificationBlock) node);
            } else {
                node.accept(this);
            }
        }

        @Override
        public void visit(Heading heading) {
            var textSelector = InterfaceUtils.generateSelector();

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

                        Label #{{label-selector}} {
                            Style: (
                                FontSize: {{font-size}},
                                RenderBold: true,
                                Wrap: true
                            );
                        }

                        {{break}}
                    }
                    """
                    .replace("{{label-selector}}", textSelector)
                    .replace("{{font-size}}", String.valueOf(fontSize))
                    .replace("{{break}}", heading.getLevel() <= 2 ? breakUI : "")
            );

            // Prepare message for heading text
            message = Message.raw("");
            visitChildren(heading);
            docsContext.getCommandBuilder().set("#" + textSelector + ".TextSpans", message);

            writer.block();
        }

        @Override
        public void visit(Paragraph paragraph) {
            // Skips padding at the bottom if it is in list and is not the last item
            boolean shouldSkipPadding =
                paragraph.getNext() instanceof BlockQuote ||
                paragraph.getNext() instanceof NotificationBlock ||
                paragraph.getNext() instanceof FencedCodeBlock ||
                paragraph.getNext() instanceof IndentedCodeBlock ||
                paragraph.getParent() instanceof ListItem ||
                paragraph.getNext() == null ||
                (paragraph.getParent() instanceof Document && paragraph.getPrevious() == null && paragraph.getNext() == null);

            boolean imageParagraph = paragraph.getFirstChild() instanceof Image;

            if (!imageParagraph) {
                var textSelector = InterfaceUtils.generateSelector();

                // Paragraph's first child isn't an image, render the paragraph with text etc.
                writer.line();
                writer.raw(
                    """
                        // TopicContentRender#visit(Paragraph) @ TestParagraph
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
            } else {
                // Paragraph with images has its children centered in the middle. Any text between those images
                // are not supported.
                writer.line();
                writer.raw(
                    """
                        // TopicContentRender#visit(Paragraph) @ ImageParagraph
                        Group {
                            Padding: (Bottom: {{bottom-padding}});
                            LayoutMode: LeftCenterWrap;
                        """
                        .replace("{{bottom-padding}}", shouldSkipPadding ? "0" : "8")
                );
                visitChildren(paragraph);
                writer.raw("}");
            }
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
                blockQuote.getPrevious() instanceof ListBlock ||
                blockQuote.getPrevious() instanceof NotificationBlock ||
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
            var codeSelector = InterfaceUtils.generateSelector();
            var literal = fencedCodeBlock.getLiteral();

            // Trim any blank lines at the bottom
            literal = literal.replaceAll("(\r?\n)+$", "");

            writer.line();

            writer.raw(
                """
                    // TopicContentRender#visit(FencedCodeBlock)
                    Group {
                        Padding: (Top: 8, Bottom: {{bottom-padding}});

                        Group {
                            LayoutMode: Top;
                            Padding: (Full: 15);
                            Background: #121a24;
                            OutlineColor: #203651;
                            OutlineSize: 2;

                            CodeEditor #{{code-selector}} {
                                Anchor: (MinWidth: 900);
                                Style: (TextColor: #cccccc, FontSize: 16, FontName: "Mono");
                                IsReadOnly: true;
                                LineNumberBackground: #000000(0.0);
                                LineNumberTextColor: #000000(0.0);
                                LineNumberWidth: 0;

                                ScrollbarStyle: {{scrollbar-style}};
                            }
                        }
                    }
                    """
                    .replace("{{code-selector}}", codeSelector)
                    .replace("{{bottom-padding}}", skipBottomPadding ? "0" : "8")
                    .replace("{{scrollbar-style}}", CommonStyles.SCROLLBAR_STYLE)
            );

            writer.block();

            docsContext.getCommandBuilder().set("#" + codeSelector + ".Value", literal);
        }

        @Override
        public void visit(IndentedCodeBlock indentedCodeBlock) {
            var skipBottomPadding = indentedCodeBlock.getNext() == null;
            var codeSelector = InterfaceUtils.generateSelector();
            var literal = indentedCodeBlock.getLiteral();

            // Trim any blank lines at the bottom
            literal = literal.replaceAll("(\r?\n)+$", "");

            writer.line();

            writer.raw(
                """
                    // TopicContentRender#visit(IndentedCodeBlock)
                    Group {
                        Padding: (Top: 8, Bottom: {{bottom-padding}});

                        Group {
                            LayoutMode: Top;
                            Padding: (Full: 15);
                            Background: #0e151d;
                            OutlineColor: #203651;
                            OutlineSize: 2;

                            CodeEditor #{{code-selector}} {
                                Style: (TextColor: #cccccc, FontSize: 16, FontName: "Mono");
                                IsReadOnly: true;
                                LineNumberBackground: #000000(0.0);
                                LineNumberTextColor: #000000(0.0);
                                LineNumberWidth: 0;

                                ScrollbarStyle: {{scrollbar-style}};
                            }
                        }
                    }
                    """
                    .replace("{{code-selector}}", codeSelector)
                    .replace("{{bottom-padding}}", skipBottomPadding ? "0" : "8")
                    .replace("{{scrollbar-style}}", CommonStyles.SCROLLBAR_STYLE)
            );

            writer.block();

            docsContext.getCommandBuilder().set("#" + codeSelector + ".Value", literal);
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
            boolean bottomPadding = listHolder == null &&
                !((bulletList.getParent() instanceof BlockQuote
                    || bulletList.getParent() instanceof NotificationBlock) && bulletList.getNext() == null);

            writer.line();
            writer.raw(
                """
                    // TopicContentRender#visit(BulletList)
                    Group {
                        Padding: (Left: 30, Bottom: {{bottom-padding}});
                        LayoutMode: Top;
                    """.replace("{{bottom-padding}}", bottomPadding ? "10" : "0")
            );
            listHolder = new BulletListHolder(listHolder, bulletList);
            visitChildren(bulletList);
            listHolder = listHolder.parent;
            writer.raw("}");
            writer.block();
        }

        @Override
        public void visit(OrderedList orderedList) {
            boolean bottomPadding = listHolder == null &&
                !((orderedList.getParent() instanceof BlockQuote
                    || orderedList.getParent() instanceof NotificationBlock) && orderedList.getNext() == null);

            writer.line();
            writer.raw(
                """
                    // TopicContentRender#visit(OrderedList)
                    Group {
                        Padding: (Left: 30, Bottom: {{bottom-padding}});
                        LayoutMode: Top;
                    """.replace("{{bottom-padding}}", bottomPadding ? "10" : "0")
            );
            listHolder = new OrderedListHolder(listHolder, orderedList);
            visitChildren(orderedList);
            listHolder = listHolder.parent;
            writer.raw("}");
            writer.block();
        }

        @Override
        public void visit(ListItem listItem) {
            var markerSelector = InterfaceUtils.generateSelector();
            var textSelector = InterfaceUtils.generateSelector();

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

        @Override
        public void visit(Image image) {
            var altText = markdownService.extractText(image);
            var resolvedAsset = runtimeImageAssetService.resolve(docsContext.getPlayerRef(), image.getDestination(), docsContext.getTopic(), docsContext.getTopic().getTopicFilePath());
            var imageSize = resolvedAsset.getImageSize();

            if (altText != null) {
                var imageResizeHintMatcher = IMAGE_RESIZE_HINT_PATTERN.matcher(altText.trim());
                if (imageResizeHintMatcher.find()) {
                    var alternativeWidth = imageResizeHintMatcher.group(1);
                    var alternativeHeight = imageResizeHintMatcher.group(2);
                    imageSize = ResizeUtils.resize(imageSize, alternativeWidth, alternativeHeight);
                    altText = imageResizeHintMatcher.replaceFirst("").trim();
                }
            }

            if (imageSize.getX() > MAX_IMAGE_SIZE) {
                imageSize = ResizeUtils.resize(imageSize, String.valueOf(MAX_IMAGE_SIZE), "0");
            }

            var tooltip = altText != null && !altText.isBlank()
                ? "TooltipText: \"%s\";".formatted(markdownService.escapeText(altText.trim()))
                : "";

            writer.line();
            writer.raw(
                """
                    // TopicContentRender#visit(Image)
                    Group {
                        Padding: (Full: 4);
                        {{tooltip}}
                        TextTooltipStyle: {{tooltip-style}};

                        Group {
                            Padding: (Full: 8);
                            Background: #121a24;
                            OutlineColor: #203651;
                            OutlineSize: 2;

                            AssetImage {
                                Anchor: (Width: {{width}}, Height: {{height}});
                                AssetPath: "{{path}}";
                            }
                        }
                    }
                    """
                    .replace("{{tooltip}}", tooltip)
                    .replace("{{tooltip-style}}", CommonStyles.TOOLTIP_STYLE_WIDE)
                    .replace("{{path}}", resolvedAsset.getCommonAssetPath())
                    .replace("{{width}}", String.valueOf(imageSize.getX()))
                    .replace("{{height}}", String.valueOf(imageSize.getY()))
            );

            writer.block();
        }

        protected void visit(TableBlock tableBlock) {
            // Prep the table group
            writer.raw(
                """
                    // TopicContentRender#visit(TableBlock)
                    Group {
                        LayoutMode: MiddleCenter;
                        Padding: (Bottom: 10);

                        Group {
                            LayoutMode: Top;
                            Padding: (Left: 2, Right: 2, Top: 2, Bottom: 3); // what. For some reason, bottom outline is cut out by 1 pixel.
                            OutlineColor: #203651;
                            OutlineSize: 2;

                """
            );

            // Visits TableHead and optionally TableBody
            visitChildren(tableBlock);

            writer.raw("}}");
            writer.block();
        }

        protected void visit(TableHead tableHead) {
            tableRenderingHead = true;
            visitChildren(tableHead);
            tableRenderingHead = false;
        }

        protected void visit(TableBody tableBody) {
            visitChildren(tableBody);
        }

        protected void visit(TableRow tableRow) {
            writer.raw(
                """
                    // TopicContentRender#visit(TableRow)
                    Group {
                        LayoutMode: Left;
                        Padding: (Bottom: 2);
                """

            );

            visitChildren(tableRow);
            writer.raw("}");
            writer.block();
        }

        protected void visit(TableCell tableCell) {
            String backgroundColor = tableRenderingHead ?
                "#0e151d"
                : "#121a24";

            var textSelector = InterfaceUtils.generateSelector();
            var cellAlignment = tableCell.getAlignment();

            if (cellAlignment == null) {
                if (tableRenderingHead) {
                    cellAlignment = Alignment.CENTER;
                } else {
                    cellAlignment = Alignment.LEFT;
                }
            }

            var contentAlignment = switch (cellAlignment) {
                case LEFT -> "Left";
                case CENTER -> "CenterMiddle";
                case RIGHT -> "Right";
            };

            writer.raw(
                """
                // TopicContentRender#visit(TableCell)
                Group {
                    Padding: (Horizontal: 15, Vertical: 10);
                    LayoutMode: {{content-alignment}};
                    FlexWeight: 1;
                    Background: {{background-color}};

                    Group {
                        Label #{{text-selector}} {
                            Anchor: (MaxWidth: 400);
                            Style: (Wrap: true, RenderBold: {{render-bold}});
                        }
                    }
                }
                """
                .replace("{{text-selector}}", textSelector)
                .replace("{{content-alignment}}", contentAlignment)
                .replace("{{render-bold}}", String.valueOf(tableRenderingHead))
                .replace("{{background-color}}", backgroundColor)
            );

            // Prepare message for any text
            message = Message.raw("");
            visitChildren(tableCell);
            docsContext.getCommandBuilder().set("#" + textSelector + ".TextSpans", message);

            writer.block();
        }

        public void visit(NotificationBlock notificationBlock) {
            boolean shouldSkipTopPadding =
                notificationBlock.getPrevious() instanceof BlockQuote ||
                notificationBlock.getPrevious() instanceof ListBlock ||
                notificationBlock.getPrevious() instanceof NotificationBlock ||
                    notificationBlock.getPrevious() instanceof Heading;

            String backgroundColor = switch (notificationBlock.getType()) {
                case INFO -> "#00529b";
                case SUCCESS -> "#4f8a10";
                case WARNING -> "#9f6000";
                case ERROR -> "#d8000c";
            };

            String outlineColor = switch (notificationBlock.getType()) {
                case INFO -> "#1a6db5";
                case SUCCESS -> "#6aad1e";
                case WARNING -> "#c47d00";
                case ERROR -> "#f21a29";
            };

            writer.line();
            writer.raw(
                """
                    // TopicContentRender#visit(NotificationBlock)
                    Group {
                        Padding: (Horizontal: 15, Bottom: 16, Top: {{top-padding}});
                        LayoutMode: Left;

                        Group {
                            LayoutMode: Top;
                            Padding: (Full: 15);
                            Background: {{background-color}}(0.4);
                            OutlineColor: {{outline-color}};
                            OutlineSize: 2;

                            Group {
                                LayoutMode: Left;
                                AssetImage {
                                    Anchor: (Top: -26, Left: -26, Width: 24, Height: 24);
                                    AssetPath: "UI/Custom/Docs/Images/{{notification-type}}-icon.png";
                                }
                            }
                    """
                    .replace("{{background-color}}", backgroundColor)
                    .replace("{{outline-color}}", outlineColor)
                    .replace("{{top-padding}}", shouldSkipTopPadding ? "0" : "16")
                    .replace("{{notification-type}}", notificationBlock.getType().name().toLowerCase())
            );

            visitChildren(notificationBlock);

            writer.raw("}}");
            writer.block();
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
                            var buttonSelector = InterfaceUtils.generateSelector();
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
                                        new EventData()
                                            .append(PageData.INTERFACE_ACTION_FIELD, InterfaceAction.OPEN_TOPIC)
                                            .append(PageData.OPEN_TOPIC_FIELD, attributeValue),
                                        true
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }

        //#endregion
    }
}
