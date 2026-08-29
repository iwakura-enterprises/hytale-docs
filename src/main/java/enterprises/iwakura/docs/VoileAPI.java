package enterprises.iwakura.docs;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.service.DocumentationService;
import enterprises.iwakura.docs.service.DocumentationViewerService;
import enterprises.iwakura.docs.service.loader.DirectDocumentationLoader;
import enterprises.iwakura.docs.service.loader.DocumentationLoader;
import enterprises.iwakura.docs.service.loader.UniversalDocumentationLoader;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class VoileAPI {

    private static VoileAPI instance;

    private final DocumentationService documentationService;
    private final DocumentationViewerService documentationViewerService;

    /**
     * Returns the {@link VoileAPI} instance
     *
     * @return {@link VoileAPI} instance
     *
     * @throws IllegalStateException if DocsAPI have not been yet loaded. This can happen when your plugin loads
     *                               before Docs is loaded.
     */
    public static VoileAPI get() {
        if (instance == null) {
            throw new IllegalStateException("DocsAPI is not yet loaded! Make sure your plugin loads AFTER Docs!");
        }
        return instance;
    }

    /**
     * Initializes DocsAPI
     */
    void init() {
        instance = this;
    }

    /**
     * Opens Voile's interface for the specified player. This respects player's last opened topic, settings, etc.
     *
     * @param playerRef Player Ref
     *
     * @return Completable future with Boolean that detonates whenever the interface was successfully opened.
     *
     * @see #openInterface(PlayerRef, String, boolean)
     */
    public CompletableFuture<Boolean> openInterface(PlayerRef playerRef) {
        return documentationViewerService.openFor(playerRef, DocsContext.empty(playerRef));
    }

    /**
     * Opens Voile's interface for the specified player with the specified topic identifier. See documentation how to
     * define a topic identifier.
     *
     * @param playerRef              Player Ref
     * @param topicIdentifier        Topic identifier
     * @param notFoundTopicIfInvalid Whenever "Not Found" topic should be displayed whenever the specified topic
     *                               identifier is invalid/unknown.
     *
     * @return Completable future with Boolean that detonates whenever the interface was successfully opened (returns
     * true even if the topic identifier is not found)
     *
     * @see #openInterface(PlayerRef)
     */
    public CompletableFuture<Boolean> openInterface(
        PlayerRef playerRef,
        String topicIdentifier,
        boolean notFoundTopicIfInvalid
    ) {
        return documentationViewerService.openFor(playerRef, Optional.ofNullable(topicIdentifier), notFoundTopicIfInvalid);
    }

    /**
     * Opens Voile's interface for the specified player with the specified {@link DocsContext}. This method is designed
     * for experienced Voile-supporting mods that want to be able to configure the
     * {@link enterprises.iwakura.docs.object.InterfaceState} in the context. Be careful when using this method!
     *
     * @param playerRef   Player Ref
     * @param docsContext Docs Context
     *
     * @return Completable future with Boolean that detonates whenever the interface was successfully opened
     *
     * @see #openInterface(PlayerRef, String, boolean)
     * @see #openInterface(PlayerRef)
     * @deprecated Unstable API, may or may not crash the server or kick the player when incorrectly used.
     */
    @Deprecated
    public CompletableFuture<Boolean> openInterface(PlayerRef playerRef, DocsContext docsContext) {
        return documentationViewerService.openFor(playerRef, docsContext);
    }

    /**
     * <p>Registers your {@link DocumentationLoader} that loads documentations.</p>
     * <p>
     * There are multiple predefined implementations of {@link DocumentationLoader} you may use:
     *     <ul>
     *         <li>
     *             {@link DirectDocumentationLoader} that loads programmatically passed list of {@link Documentation}.
     *              The easiest way to register documentations.
     *          </li>
     *          <li>
     *          {@link UniversalDocumentationLoader} that loads the documentation from specified index {@link java.nio.file.Path}
     *          </li>
     *     </ul>
     * </p>
     *
     * @param plugin              Your plugin instance
     * @param documentationLoader Non-null {@link DocumentationLoader}
     *
     * @see #register(JavaPlugin, Documentation)
     * @see #register(JavaPlugin, List)
     */
    public void register(@NonNull JavaPlugin plugin, @NonNull DocumentationLoader documentationLoader) {
        documentationService.registerDocumentationLoader(plugin, documentationLoader);
    }

    /**
     * Registers specified documentation programmatically. Uses {@link DirectDocumentationLoader}.
     *
     * @param plugin        Your plugin instance
     * @param documentation Non-null documentation
     */
    public void register(@NonNull JavaPlugin plugin, @NonNull Documentation documentation) {
        this.register(plugin, new DirectDocumentationLoader(List.of(documentation)));
    }

    /**
     * Registers specified documentations programmatically using {@link DirectDocumentationLoader}.
     *
     * @param plugin        Your plugin instance
     * @param documentation Non-null list of documentations
     */
    public void register(@NonNull JavaPlugin plugin, @NonNull List<Documentation> documentation) {
        this.register(plugin, new DirectDocumentationLoader(documentation));
    }
}
