package enterprises.iwakura.docs;

import java.util.List;

import com.hypixel.hytale.event.IBaseEvent;

import enterprises.iwakura.docs.command.DocsCommand;
import enterprises.iwakura.docs.command.ReloadCommand;
import enterprises.iwakura.docs.listener.BaseGlobalListener;
import enterprises.iwakura.docs.service.ConfigurationService;
import enterprises.iwakura.docs.service.DocumentationService;
import enterprises.iwakura.docs.service.PluginAssetLoaderService;
import enterprises.iwakura.docs.service.ServerService;
import enterprises.iwakura.docs.service.UpdateCheckerService;
import enterprises.iwakura.docs.service.ValidatorService;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class Docs {

    private final List<BaseGlobalListener<?>> globalListeners;

    private final DocsCommand docsCommand;
    private final ReloadCommand reloadCommand;

    private final VoileAPI voileAPI;
    private final ConfigurationService configurationService;
    private final DocumentationService documentationService;
    private final ValidatorService validatorService;
    private final PluginAssetLoaderService pluginAssetLoaderService;
    private final UpdateCheckerService updateCheckerService;
    private final ServerService serverService;

    private final DocsPlugin plugin;
    private final Logger logger;

    public void setup() {
        logger.info("Initializing Voile...");
        logger.info("Made by Iwakura Enterprises");

        configurationService.init();
        validatorService.init(plugin.getDataDirectory());
        updateCheckerService.init();

        if (!serverService.isRunningOnDedicatedServer()) {
            logger.info("We're running in singleplayer! Disabling OOBE...");
            configurationService.getDocsConfig().setOutOfBoxExperience(false);
        }

        logger.info("Registering commands...");
        docsCommand.initAliases();
        plugin.getCommandRegistry().registerCommand(docsCommand);
        plugin.getCommandRegistry().registerCommand(reloadCommand);

        logger.info("Registering global listeners...");
        globalListeners.forEach(this::registerGlobalListener);

        logger.info("Initializing VoileAPI...");
        voileAPI.init();

        logger.info("Registering documentation loaders...");
        documentationService.registerDocumentationLoaders();
    }

    public void start() {
        pluginAssetLoaderService.scanForDocumentationIndex();
        pluginAssetLoaderService.scanForDocumentationMarkdown();
        documentationService.reloadDocumentations();
    }

    public void shutdown() {

    }

    /**
     * Helper method that registers a global listener
     *
     * @param listener the listener to register
     * @param <K>      the event key type
     * @param <E>      the event type
     */
    private <K, E extends IBaseEvent<K>> void registerGlobalListener(BaseGlobalListener<?> listener) {
        //noinspection unchecked
        var typedListener = (BaseGlobalListener<E>) listener;
        plugin.getEventRegistry().registerGlobal(typedListener.getEventClass(), typedListener::onEvent);
    }
}
