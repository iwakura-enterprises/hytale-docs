package enterprises.iwakura.docs.object;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Specific mods for the interface that specifies the available documentation types.
 */
@Getter
@RequiredArgsConstructor
public enum InterfaceMode {
    VOILE("Voile", "voile-icon.png", List.of(DocumentationType.SERVER, DocumentationType.MOD, DocumentationType.INTERNAL)),
    HYTALE_MODDING_WIKI("Hytale Modding Wiki", "hytale-modding-icon.png", List.of(DocumentationType.HYTALE_MODDING_WIKI_INSTALLED, DocumentationType.HYTALE_MODDING_WIKI));

    private final String userFriendlyName;
    private final String logoName;
    private final List<DocumentationType> documentationTypes;

    public static final List<InterfaceMode> ALL = Arrays.asList(InterfaceMode.values());

    public boolean has(DocumentationType type) {
        return documentationTypes.contains(type);
    }

    public static Optional<InterfaceMode> forType(DocumentationType type) {
        return ALL.stream().filter(mode -> mode.has(type)).findFirst();
    }
}
