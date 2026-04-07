package enterprises.iwakura.docs.integration.hytalemodding;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import enterprises.iwakura.docs.DocsPlugin;
import enterprises.iwakura.docs.VoileAPI;
import enterprises.iwakura.docs.integration.hytalemodding.api.HMWikiApi;
import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.DocumentationType;
import enterprises.iwakura.docs.object.InterfaceMode;
import enterprises.iwakura.docs.service.ConfigurationService;
import enterprises.iwakura.docs.service.DocumentationService;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class HMWikiService {

    public static final String HM_WIKI_CACHE_DIRECTORY = "integration/hytale-modding-wiki";
    public static final List<DocumentationType> DOCUMENTATION_TYPES_TO_PRELOAD = List.of(
        DocumentationType.EXTERNAL_MOD,
        DocumentationType.HYTALE_MODDING_WIKI_INSTALLED
    );

    private static final Timer timer = new Timer();

    private final HMWikiDocumentationLoader hmWikiDocumentationLoader;

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

        VoileAPI.get().register(plugin, hmWikiDocumentationLoader);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    checkAndPreloadMod();
                } catch (Exception exception) {
                    logger.error("Failed to preload mod pages!", exception);
                }
            }
        }, 0, 10_000); // 10 seconds
    }

    private void checkAndPreloadMod() {
        var config = configurationService.getDocsConfig().getIntegration().getHytaleModdingWiki();

        if (!config.isEnabled() || !config.isPreLoadModsInBackground()) {
            return;
        }

        for (Documentation documentation : documentationService.getDocumentations(DOCUMENTATION_TYPES_TO_PRELOAD)) {
            var mod = documentation.getAdditionalInfo().getHytaleModdingWikiMod();

            if (mod != null && hmWikiDocumentationLoader.preloadDocumentation(documentation, mod, false, false) != null) {
                break;
            }
        }
    }

    private Path getCacheDirectory() {
        return plugin.getDataDirectory().resolve(HM_WIKI_CACHE_DIRECTORY);
    }
}
