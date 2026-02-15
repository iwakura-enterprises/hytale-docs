package enterprises.iwakura.docs.ui;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CommandStyles {

    public static final String NORMAL_BUTTON_STYLE =
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

    public static final String SELECTED_BUTTON_STYLE =
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

}
