package enterprises.iwakura.docs;

import java.util.List;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;

import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.service.DocumentationService;
import enterprises.iwakura.docs.service.loader.DirectDocumentationLoader;
import enterprises.iwakura.docs.service.loader.DocumentationLoader;
import enterprises.iwakura.docs.service.loader.ResourcesDocumentationLoader;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class DocsAPI {

    private static DocsAPI instance;

    private final Logger logger;
    private final DocumentationService documentationService;

    /**
     * Returns the {@link DocsAPI} instance
     *
     * @return {@link DocsAPI} instance
     *
     * @throws IllegalStateException if DocsAPI have not been yet loaded. This can happen when your plugin loads
     *                               before Docs is loaded.
     */
    public static DocsAPI get() {
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

    // TODO: Specify proper documentation page

    /**
     * <p>Registers your {@link DocumentationLoader} that loads documentations.</p>
     * <p>
     * There are multiple predefined implementations of {@link DocumentationLoader} you may use:
     *     <ul>
     *         <li>
     *             {@link DirectDocumentationLoader} that loads programmatically passed list of {@link Documentation}.
     *              The easiest way to register documentations.
     *              </li>
     *         <li>
     *             {@link ResourcesDocumentationLoader} that loads documentations from specific index.json inside your
     *             plugin's resources. This allows you to create Markdown files right in the resources. See
     *             <a href="https://docs.iwakura.enterprises/hytale-docs.html">documentation page</a>
     *             or its javadocs for
     *             more info, as it requires specific setup.
     *         </li>
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
