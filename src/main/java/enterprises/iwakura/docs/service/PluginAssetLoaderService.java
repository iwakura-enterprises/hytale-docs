package enterprises.iwakura.docs.service;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;

import enterprises.iwakura.docs.DocsPlugin;
import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.DocumentationType;
import enterprises.iwakura.docs.service.loader.ResourceMarkdownFileDocumentationLoader;
import enterprises.iwakura.docs.service.loader.ResourcesDocumentationLoader;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class PluginAssetLoaderService {

    public static final String DOCUMENTATION_INDEX_RESOURCE_PATH = "Common/Docs/%s_%s.json";
    public static final String MARKDOWN_FILE_RESOURCE_PATH = "Common/Docs/%s_%s.md";

    private final DocumentationService documentationService;
    private final Logger logger;
    private final DocsPlugin plugin;

    /**
     * Scans currently loaded plugins for documentations
     */
    public void scanForDocumentationIndex() {
        logger.info("Scanning plugins for documentation indexes...");

        for (PluginBase plugin : PluginManager.get().getPlugins()) {
            if (plugin instanceof JavaPlugin javaPlugin) {
                var identifier = plugin.getIdentifier();
                var indexResourceUrl = javaPlugin.getClassLoader().getResource(
                    DOCUMENTATION_INDEX_RESOURCE_PATH.formatted(identifier.getGroup(), identifier.getName()
                ));
                if (indexResourceUrl != null) {
                    logger.info("Found AssetDocumentationIndexConfig resource in plugin %s, registering resource documentation loader...".formatted(
                        plugin.getName()
                    ));
                    String indexPath = indexResourceUrl.getPath();
                    int jarSeparator = indexPath.indexOf("!/");
                    if (jarSeparator != -1) {
                        indexPath = indexPath.substring(jarSeparator + 2);
                    }
                    documentationService.registerDocumentationLoader(javaPlugin, new ResourcesDocumentationLoader(
                        DocumentationType.MOD,
                        javaPlugin.getClassLoader(),
                        indexPath
                    ));
                }
            }
        }
    }

    /**
     * Scans currently loaded plugins for simple markdown documentations
     */
    public void scanForDocumentationMarkdown() {
        logger.info("Scanning plugins for documentation markdown...");

        var markdownFileResources = new HashMap<JavaPlugin, URL>();

        for (PluginBase plugin : PluginManager.get().getPlugins()) {
            if (plugin instanceof JavaPlugin javaPlugin) {
                var identifier = plugin.getIdentifier();
                var markdownFileResourceUrl = javaPlugin.getClassLoader().getResource(
                    MARKDOWN_FILE_RESOURCE_PATH.formatted(identifier.getGroup(), identifier.getName()
                    ));
                if (markdownFileResourceUrl != null) {
                    logger.info("Found Markdown file resource in plugin %s".formatted(
                        plugin.getName()
                    ));
                    markdownFileResources.put(javaPlugin, markdownFileResourceUrl);
                }
            }
        }

        logger.info("Found %d markdown file resources".formatted(markdownFileResources.size()));

        if (!markdownFileResources.isEmpty()) {
            logger.info("Registering ResourceMarkdownFileDocumentationLoader...");
            documentationService.registerDocumentationLoader(plugin, new ResourceMarkdownFileDocumentationLoader(
                () -> Documentation.builder()
                    .group("Voile")
                    .id("GeneratedDocs")
                    .name("Various Mods")
                    .sortIndex(999999)
                    .type(DocumentationType.MOD)
                    .build(),
                markdownFileResources
            ));
        }
    }
}
