package enterprises.iwakura.docs.ui.render;

import java.util.List;

import enterprises.iwakura.docs.Version;
import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.docs.ui.render.DocumentationViewerRenderer.RenderData;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class DocumentationViewerRenderer implements Renderer<RenderData> {

    private final HytaleCommonRenderer hytaleCommonRenderer;
    private final DocumentationTreeRenderer documentationTreeRenderer;
    private final TopicRenderer topicRenderer;
    private final TopicChapterTreeRenderer topicChapterTreeRenderer;

    @Override
    public String render(DocsContext ctx, RenderData renderData) {
        var hytaleCommonUI = hytaleCommonRenderer.render(ctx, null);

        var documentationTreeUI = documentationTreeRenderer.render(ctx, renderData.getDocumentations());
        var topicUI = topicRenderer.render(ctx, renderData.getTopic());
        var topicChapterTreeUI = topicChapterTreeRenderer.render(ctx, renderData.getTopic());

        return
            """
            {{hytale-common}}

            // PageOverlay darkens the background
            @PageOverlay {

                // Classic Hytale container
                @Container #WindowContainer {
                    Anchor: (Width: 1850, Height: 990);
                    

                    // Title for the container (from @Container)
                    #Title {
                        Group #ContainerTitleGroup {
                            @Title {
                                @Text = "Voile // {{topic-title}}";
                            }
                        }
                    }

                    // Content for the container (from @Container)
                    #Content {
                        Group {
                            LayoutMode: Left;

                            {{documentation-tree}}
                            {{topic}}
                            {{chapter-tree}}
                        }
                    }
                }
            }
            
            Group {
                LayoutMode: Top;
                Anchor: (Left: 5, Top: 5);
                Label {
                    Text: "Voile {{version}} // Made by Iwakura Enterprises";
                    Style: (
                        TextColor: #b4c8c9(0.3),
                        RenderBold: true,
                        RenderUppercase: true
                    );
                }
                Label {
                    Text: "mayuna@iwakura.enterprises";
                    Style: (
                        TextColor: #b4c8c9(0.3),
                        FontSize: 13,
                        FontName: "Mono"
                    );
                }
            }
            
            // Adds UX back button to the bottom left of the screen
            @BackButton {}
            """
                .replace("{{hytale-common}}", hytaleCommonUI)
                .replace("{{documentation-tree}}", documentationTreeUI)
                .replace("{{topic}}", topicUI)
                .replace("{{topic-title}}", renderData.getTopic().getName())
                .replace("{{chapter-tree}}", topicChapterTreeUI)
                .replace("{{version}}", Version.VERSION);
    }

    @Data
    public static class RenderData {

        private final List<Documentation> documentations;
        private final Topic topic;
    }
}
