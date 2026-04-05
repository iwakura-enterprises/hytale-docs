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
    SERVER("Server Wiki", true),
    /**
     * Mod-specific documentation. This is the default type for documentation supplied by mods.
     */
    MOD("Mod Wiki", false),
    /**
     * Mod-specific documentation that is loaded from external integration but shown in the main interface mode.
     */
    EXTERNAL_MOD("External Mod Wiki", false),
    /**
     * Hytale modding wiki type for installed mods
     */
    HYTALE_MODDING_WIKI_INSTALLED("Installed Mods", false),
    /**
     * Hytale modding wiki
     */
    HYTALE_MODDING_WIKI("Mods", false),
    /**
     * Reserved for internal documentation.
     */
    INTERNAL("Internal Wiki", false);

    private final String humanReadable;
    private final boolean allowSystemFileSystemRead;

    /**
     * All documentation types
     */
    public static final List<DocumentationType> ALL = Arrays.asList(DocumentationType.values());

}
