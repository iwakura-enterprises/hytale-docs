package enterprises.iwakura.docs.ui;

import org.jspecify.annotations.NonNull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import enterprises.iwakura.docs.Version;
import enterprises.iwakura.docs.service.DebugService;
import enterprises.iwakura.docs.service.FileSystemCacheService;
import enterprises.iwakura.docs.ui.AboutVoilePage.PageData;
import lombok.Data;

public class AboutVoilePage extends InteractiveCustomUIPage<PageData> {

    public static final String MAIN_CONTENT_SELECTOR = "#Content";

    private final boolean showDebug;
    private final DebugService debugService;

    public AboutVoilePage(@NonNull PlayerRef playerRef, boolean showDebug, DebugService debugService) {
        super(playerRef, CustomPageLifetime.CanDismiss, PageData.CODEC);
        this.showDebug = showDebug;
        this.debugService = debugService;
    }

    @Override
    public void build(
        @NonNull Ref<EntityStore> ref,
        @NonNull UICommandBuilder commandBuilder,
        @NonNull UIEventBuilder eventBuilder,
        @NonNull Store<EntityStore> store
    ) {
        commandBuilder.append("Docs/Pages/AboutVoilePage.ui");
        commandBuilder.appendInline(MAIN_CONTENT_SELECTOR,
            """
            Group {
                SceneBlur {}
                Group { Background: #000000(0.45); }

                Group {
                    LayoutMode: MiddleCenter;

                    Group {
                        AssetImage {
                            Anchor: (Width: 1399 * 0.7, Height: 832 * 0.7);
                            AssetPath: "UI/Custom/Docs/Images/voile-about-background.png";
                        }
                    }
                }

                Group {
                    LayoutMode: MiddleCenter;

                    CodeEditor #DebugCodeEditor {
                        Anchor: (Left: -315, Top: 135, Width: 530, Height: 340);
                        Style: (TextColor: #FAD7E9, FontSize: 16, FontName: "Mono");
                        IsReadOnly: true;
                        LineNumberBackground: #000000(0.0);
                        LineNumberTextColor: #000000(0.0);
                        LineNumberWidth: 0;

                        ScrollbarStyle: {{scrollbar-style}};
                    }
                }
            }
            """.replace("{{scrollbar-style}}", CommonStyles.SCROLLBAR_STYLE)
        );
        commandBuilder.set("#DebugCodeEditor.Value", createDebugContent());
    }

    private String createDebugContent() {
        StringBuilder content = new StringBuilder();
        content.append("Voile ").append(Version.VERSION).append("\n");
        content.append("============\n");
        content.append("Made by Iwakura Enterprises\n");
        content.append("\n");

        if (showDebug) {
            content.append("[!] This content may contain sensitive data. Do not share with people you don't trust. API tokens and other secrets are REDACTED.\n");
            content.append("\n");
            content.append(debugService.createDebugString());
        } else {
            content.append("    Create stunning in-game wikis.\n");
            content.append("\n");
            content.append("* Download: https://download.voile.dev\n");
            content.append("* Website: https://voile.dev\n");
            content.append("* Documentation: https://docs.voile.dev\n");
            content.append("* Support: https://support.voile.dev\n");
            content.append("* Support e-mail: mauyna@iwakura.enterprises\n");
            content.append("* Roadmap: https://youtrack.iwakura.enterprises\n");
            content.append("\n");
            content.append("\n");
            content.append("\n");
            content.append("\n");
            content.append("\n");
            content.append("[>] Voile is licensed under the MIT License\n");
            content.append("[>] Drawing made by mayuna\n");
            content.append("[>] Patchouli Knowledge is a character from Touhou Project made by Team Shanghai Alice. This project is not affiliated with Team Shanghai Alice in any way.\n");
            content.append("[>] Sublimia Syndrome is a book written By Exurb1a. This project is not affiliated with Exurb1a in any way.\n");
            content.append("[>] Voile may serve you content made by other creators; Voile does not endorse, verify, or take responsibility for any user-created content. All content is provided as-is, without any warranty or guarantee. Voile and Iwakura Enterprises are not liable for any damages arising from the use of user-created content.\n");
            content.append("\n");
            content.append("\n");
            content.append("Run /debug-voile command for more information.");
        }

        return content.toString();
    }

    @Override
    public void handleDataEvent(
        @NonNull Ref<EntityStore> ref,
        @NonNull Store<EntityStore> store,
        AboutVoilePage.@NonNull PageData data
    ) {

    }

    @Data
    public static class PageData {

        public static final String INTERFACE_ACTION_FIELD = "InterfaceAction";

        public static final BuilderCodec<PageData> CODEC = BuilderCodec.builder(
                PageData.class, PageData::new)
            .append(new KeyedCodec<>(INTERFACE_ACTION_FIELD, Codec.STRING), PageData::setInterfaceActionValue, PageData::getInterfaceActionValue).add()
            .build();

        private String interfaceActionValue;

        public PageData.InterfaceAction getInterfaceAction() {
            return interfaceActionValue != null ? PageData.InterfaceAction.valueOf(interfaceActionValue) : null;
        }

        public enum InterfaceAction {
            CLOSE;
        }
    }
}
