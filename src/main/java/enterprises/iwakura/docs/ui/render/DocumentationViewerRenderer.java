package enterprises.iwakura.docs.ui.render;

import java.util.List;

import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;

import enterprises.iwakura.docs.Version;
import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.docs.service.MarkdownService;
import enterprises.iwakura.docs.service.UpdateCheckerService;
import enterprises.iwakura.docs.ui.CommonStyles;
import enterprises.iwakura.docs.ui.DocumentationViewerPage.PageData;
import enterprises.iwakura.docs.ui.DocumentationViewerPage.PageData.InterfaceAction;
import enterprises.iwakura.docs.ui.render.DocumentationViewerRenderer.RenderData;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class DocumentationViewerRenderer implements Renderer<RenderData> {

    public static final String ABOUT_VOILE_BUTTON_SELECTOR = "#AboutVoileButton";

    private final HytaleCommonRenderer hytaleCommonRenderer;
    private final DocumentationTreeRenderer documentationTreeRenderer;
    private final TopicRenderer topicRenderer;
    private final TopicChapterTreeRenderer topicChapterTreeRenderer;
    private final UpdateCheckerService updateCheckerService;
    private final MarkdownService markdownService;

    @Override
    public String render(DocsContext ctx, RenderData renderData) {
        var hytaleCommonUI = hytaleCommonRenderer.render(ctx, null);

        var documentationTreeUI = documentationTreeRenderer.render(ctx, ctx.getDocumentations());
        var topicUI = topicRenderer.render(ctx, renderData.getTopic());
        var topicChapterTreeUI = topicChapterTreeRenderer.render(ctx, renderData.getTopic());

        ctx.getEventBuilder().addEventBinding(
            CustomUIEventBindingType.Activating,
            ABOUT_VOILE_BUTTON_SELECTOR,
            new EventData().append(PageData.INTERFACE_ACTION_FIELD, InterfaceAction.OPEN_ABOUT_VOILE_PAGE.name()),
            false
        );

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
                Group {
                    LayoutMode: Left;

                    TextButton {{about-voile-button-selector}} {
                        Text: "Voile {{version}}";
                        Style: (
                            Default: (LabelStyle: (FontSize: 17, RenderBold: true, RenderUppercase: false, HorizontalAlignment: Center, VerticalAlignment: Center, TextColor: #bfbfbf)),
                            Hovered: (LabelStyle: (FontSize: 17, RenderBold: true, RenderUppercase: false, HorizontalAlignment: Center, VerticalAlignment: Center, TextColor: #ffffffff)),
                            Pressed: (LabelStyle: (FontSize: 17, RenderBold: true, RenderUppercase: false, HorizontalAlignment: Center, VerticalAlignment: Center, TextColor: #e1e1e1)),
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
                        TooltipText: "Show information about Voile";
                        TextTooltipStyle: {{interface-button-tooltip-style-short}};
                        TextTooltipShowDelay: 0.1;
                    }

                    Group { Padding: (Left: 10); }

                    Label {
                        Text: "{{update-available-text}}";
                        Style: (
                            TextColor: #8a90f0,
                            RenderBold: true,
                            RenderUppercase: true
                        );
                    }
                }
            }

            // Adds UX back button to the bottom left of the screen
            @BackButton {}
            """
                .replace("{{about-voile-button-selector}}", ABOUT_VOILE_BUTTON_SELECTOR)
                .replace("{{hytale-common}}", hytaleCommonUI)
                .replace("{{documentation-tree}}", documentationTreeUI)
                .replace("{{topic}}", topicUI)
                .replace("{{topic-title}}", markdownService.escapeText(renderData.getTopic().getName()))
                .replace("{{chapter-tree}}", topicChapterTreeUI)
                .replace("{{version}}", Version.VERSION)
                .replaceAll("\\{\\{interface-button-tooltip-style-short}}", CommonStyles.TOOLTIP_STYLE_SHORT)
                .replace("{{update-available-text}}", updateCheckerService.isUpdateAvailable() ? markdownService.escapeText("Update available: " + updateCheckerService.getUpdateVersion()) : "");
    }

    @Data
    public static class RenderData {

        private final List<Documentation> documentations;
        private final Topic topic;
    }
}
