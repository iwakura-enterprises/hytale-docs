package enterprises.iwakura.docs.ui;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CommonStyles {

    public static final String NORMAL_TOPIC_BUTTON_STYLE =
        """
        (
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
        """;

    public static final String SELECTED_TOPIC_BUTTON_STYLE =
        """
        (
            Default: (LabelStyle: (TextColor: #fcca4c, Wrap: true)),
            Hovered: (LabelStyle: (TextColor: #f5dfa9, Wrap: true)),
            Pressed: (LabelStyle: (TextColor: #c9a13c, Wrap: true)),
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
        """;

    public static final String MATCHES_SEARCH_TOPIC_BUTTON_STYLE =
        """
        (
            Default: (LabelStyle: (TextColor: #9fdfed, Wrap: true)),
            Hovered: (LabelStyle: (TextColor: #dff5ff, Wrap: true)),
            Pressed: (LabelStyle: (TextColor: #b2c4cc, Wrap: true)),
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
        """;

    public static final String INTERFACE_BUTTON_STYLE =
        """
        (
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
        )
        """;

    public static final String TOOLTIP_STYLE =
        """
        (
            Background: (TexturePath: "Common/TooltipDefaultBackground.png", Border: 24),
            MaxWidth: 500,
            LabelStyle: (Wrap: true, FontSize: 16),
            Padding: 24
        )
        """;

    public static final String SCROLLBAR_STYLE =
        """
        (
            Spacing: 6,
            Size: 6,
            Background: (TexturePath: "Common/Scrollbar.png", Border: 3),
            Handle: (TexturePath: "Common/ScrollbarHandle.png", Border: 3),
            HoveredHandle: (TexturePath: "Common/ScrollbarHandleHovered.png", Border: 3),
            DraggedHandle: (TexturePath: "Common/ScrollbarHandleDragged.png", Border: 3),
            OnlyVisibleWhenHovered: false
        )
        """;
}
