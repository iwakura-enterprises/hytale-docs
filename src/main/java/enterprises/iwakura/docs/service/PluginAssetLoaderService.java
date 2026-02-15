package enterprises.iwakura.docs.service;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;

import enterprises.iwakura.docs.object.DocumentationType;
import enterprises.iwakura.docs.service.loader.ResourcesDocumentationLoader;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class PluginAssetLoaderService {

    public static final String DOCS_RESOURCE_PATH = "Common/Docs/%s_%s.json";

    private final DocumentationService documentationService;
    private final Logger logger;

    /**
     * Scans currently loaded plugins for documentations
     */
    public void scan() {
        logger.info("Scanning plugins for documentations...");

        for (PluginBase plugin : PluginManager.get().getPlugins()) {
            if (plugin instanceof JavaPlugin javaPlugin) {
                var identifier = plugin.getIdentifier();
                var indexResourceUrl = javaPlugin.getClassLoader().getResource(
                    DOCS_RESOURCE_PATH.formatted(identifier.getGroup(), identifier.getName()
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
}
