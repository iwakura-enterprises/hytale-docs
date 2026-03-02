package enterprises.iwakura.docs.integration.hytalemodding;

import java.nio.file.Files;
import java.nio.file.Path;

import enterprises.iwakura.docs.DocsPlugin;
import enterprises.iwakura.docs.VoileAPI;
import enterprises.iwakura.docs.api.hytalemodding.HMWikiApi;
import enterprises.iwakura.docs.service.ConfigurationService;
import enterprises.iwakura.docs.service.DocumentationService;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class HMWikiService {

    public static final String HM_WIKI_CACHE_DIRECTORY = "integration/hytale-modding-wiki";

    private final ConfigurationService configurationService;
    private final DocumentationService documentationService;
    private final HMWikiApi hmWikiApi;
    private final VoileAPI voileAPI;

    private final DocsPlugin plugin;
    private final Logger logger;

    /**
     * Initializes HytaleModding Wiki service
     */
    public void init() {
        logger.info("Initializing HytaleModding Wiki integration...");
        var config = configurationService.getDocsConfig().getIntegration().getHytaleModdingWiki();

        if (!config.isEnabled()) {
            logger.warn("HytaleModding Wiki integration is disabled.");
            return;
        }

        var cacheDirectory = getCacheDirectory();
        if (!Files.exists(cacheDirectory)) {
            try {
                Files.createDirectories(cacheDirectory);
            } catch (Exception exception) {
                logger.error("Failed to create HM Wiki cache directory: " + cacheDirectory, exception);
            }
        }

        VoileAPI.get().register(plugin, new HMWikiDocumentationLoader(this, hmWikiApi));
    }

    private Path getCacheDirectory() {
        return plugin.getDataDirectory().resolve(HM_WIKI_CACHE_DIRECTORY);
    }
}
