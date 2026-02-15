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
    SERVER("Server"),
    /**
     * Mod-specific documentation. This is the default type for documentation supplied by mods.
     */
    MOD("Mod"),
    /**
     * Reserved for internal documentation.
     */
    INTERNAL("Internal");

    private final String humanReadable;

    /**
     * All documentation types
     */
    public static final List<DocumentationType> ALL = Arrays.asList(DocumentationType.values());
}
