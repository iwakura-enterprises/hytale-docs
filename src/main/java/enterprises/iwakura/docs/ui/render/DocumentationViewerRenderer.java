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
    public static final String TOGGLE_ULTRA_WIDESCREEN_SUPPORT_BUTTON_SELECTOR = "#ToggleUltraWidescreenSupportButton";
    public static final String TOGGLE_SMALLER_MAX_IMAGE_SIZE_BUTTON_SELECTOR = "#ToggleSmallerMaxImageSizeButton";
    public static final String ULTRA_WIDESCREEN_ANCHOR = "Width: 1850, Height: 990";
    public static final String DYNAMIC_ANCHOR = "Full: 50";

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

        ctx.getEventBuilder().addEventBinding(
            CustomUIEventBindingType.Activating,
            TOGGLE_ULTRA_WIDESCREEN_SUPPORT_BUTTON_SELECTOR,
            new EventData().append(PageData.INTERFACE_ACTION_FIELD, InterfaceAction.TOGGLE_ULTRA_WIDESCREEN_SUPPORT.name()),
            true
        );

        ctx.getEventBuilder().addEventBinding(
            CustomUIEventBindingType.Activating,
            TOGGLE_SMALLER_MAX_IMAGE_SIZE_BUTTON_SELECTOR,
            new EventData().append(PageData.INTERFACE_ACTION_FIELD, InterfaceAction.TOGGLE_SMALLER_MAX_IMAGE_SIZE.name()),
            true
        );

        return
            """
            {{hytale-common}}

            // PageOverlay darkens the background
            @PageOverlay {

                // Classic Hytale container
                @Container #WindowContainer {
                    Anchor: ({{viewer-anchor}});


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
            
            Group {
                LayoutMode: Bottom;
                Anchor: (Right: 5, Bottom: 5);
                Group {
                    LayoutMode: Right;
                    
                    TextButton {{toggle-ultra-widescreen-support-button-selector}} {
                        Text: "Toggle ultra widescreen support";
                        Style: (
                            Default: (LabelStyle: (FontSize: 12, RenderBold: false, RenderUppercase: false, HorizontalAlignment: Center, VerticalAlignment: Center, TextColor: #bfbfbf(0.5))),
                            Hovered: (LabelStyle: (FontSize: 12, RenderBold: false, RenderUppercase: false, HorizontalAlignment: Center, VerticalAlignment: Center, TextColor: #ffffff(0.5))),
                            Pressed: (LabelStyle: (FontSize: 12, RenderBold: false, RenderUppercase: false, HorizontalAlignment: Center, VerticalAlignment: Center, TextColor: #e1e1e1(0.5))),
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
                        TooltipText: "Currently {{is-ultra-widescreen-support-enabled}}";
                        TextTooltipStyle: {{interface-button-tooltip-style-short}};
                        TextTooltipShowDelay: 0.1;
                    }
                    
                    Group {
                        Anchor: (Width: 10);
                    }
    
                    TextButton {{toggle-smaller-max-image-size-button-selector}} {
                        Text: "Toggle smaller images";
                        Style: (
                            Default: (LabelStyle: (FontSize: 12, RenderBold: false, RenderUppercase: false, HorizontalAlignment: Center, VerticalAlignment: Center, TextColor: #bfbfbf(0.5))),
                            Hovered: (LabelStyle: (FontSize: 12, RenderBold: false, RenderUppercase: false, HorizontalAlignment: Center, VerticalAlignment: Center, TextColor: #ffffff(0.5))),
                            Pressed: (LabelStyle: (FontSize: 12, RenderBold: false, RenderUppercase: false, HorizontalAlignment: Center, VerticalAlignment: Center, TextColor: #e1e1e1(0.5))),
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
                        TooltipText: "Currently {{is-smaller-max-image-size-enabled}}";
                        TextTooltipStyle: {{interface-button-tooltip-style-short}};
                        TextTooltipShowDelay: 0.1;
                    }
                }
            }
            
            """
                .replace("{{about-voile-button-selector}}", ABOUT_VOILE_BUTTON_SELECTOR)
                .replace("{{toggle-ultra-widescreen-support-button-selector}}", TOGGLE_ULTRA_WIDESCREEN_SUPPORT_BUTTON_SELECTOR)
                .replace("{{toggle-smaller-max-image-size-button-selector}}", TOGGLE_SMALLER_MAX_IMAGE_SIZE_BUTTON_SELECTOR)
                .replace("{{hytale-common}}", hytaleCommonUI)
                .replace("{{documentation-tree}}", documentationTreeUI)
                .replace("{{topic}}", topicUI)
                .replace("{{topic-title}}", markdownService.escapeText(renderData.getTopic().getName()))
                .replace("{{chapter-tree}}", topicChapterTreeUI)
                .replace("{{version}}", Version.VERSION)
                .replaceAll("\\{\\{interface-button-tooltip-style-short}}", CommonStyles.TOOLTIP_STYLE_SHORT)
                .replace("{{viewer-anchor}}", ctx.getInterfaceState().isUltraWidescreenSupport() ? ULTRA_WIDESCREEN_ANCHOR : DYNAMIC_ANCHOR)
                .replace("{{is-ultra-widescreen-support-enabled}}", ctx.getInterfaceState().isUltraWidescreenSupport() ? "enabled" : "disabled")
                .replace("{{is-smaller-max-image-size-enabled}}", ctx.getInterfaceState().isSmallerMaxImageSize() ? "enabled" : "disabled")
                .replace("{{update-available-text}}", updateCheckerService.isUpdateAvailable() ? markdownService.escapeText("Update available: " + updateCheckerService.getUpdateVersion()) : "");
    }

    @Data
    public static class RenderData {

        private final List<Documentation> documentations;
        private final Topic topic;
    }
}
