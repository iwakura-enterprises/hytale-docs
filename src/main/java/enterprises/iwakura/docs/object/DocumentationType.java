package enterprises.iwakura.docs.object;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Type for the {@link Documentation}. This type allows server owners to filter their documentation list for players.
 */
@Getter
@RequiredArgsConstructor
public enum DocumentationType {
    /**
     * Server-specific documentation. This is the default type for documentation made by the server owner.
     */
    SERVER("Server wiki"),
    /**
     * Mod-specific documentation. This is the default type for documentation supplied by mods.
     */
    MOD("Mod wiki"),
    /**
     * Hytale modding wiki type for installed mods
     */
    HYTALE_MODDING_WIKI_INSTALLED("Installed Mods"),
    /**
     * Hytale modding wiki
     */
    HYTALE_MODDING_WIKI("Mods"),
    /**
     * Reserved for internal documentation.
     */
    INTERNAL("Internal docs");

    private final String humanReadable;

    /**
     * All documentation types
     */
    public static final List<DocumentationType> ALL = Arrays.asList(DocumentationType.values());

}
