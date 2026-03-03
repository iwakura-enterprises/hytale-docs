package enterprises.iwakura.docs;

import java.util.List;
import java.util.UUID;

import com.al3x.HStats;
import com.hypixel.hytale.event.IBaseEvent;

import enterprises.iwakura.docs.command.CommandShortcutCommand;
import enterprises.iwakura.docs.command.DocsCommand;
import enterprises.iwakura.docs.command.ReloadCommand;
import enterprises.iwakura.docs.config.DocsConfig.CommandShortcuts.Command;
import enterprises.iwakura.docs.listener.BaseGlobalListener;
import enterprises.iwakura.docs.service.ConfigurationService;
import enterprises.iwakura.docs.service.DocumentationService;
import enterprises.iwakura.docs.service.ImageService;
import enterprises.iwakura.docs.service.PluginAssetLoaderService;
import enterprises.iwakura.docs.service.RuntimeImageAssetService;
import enterprises.iwakura.docs.service.ServerService;
import enterprises.iwakura.docs.service.UpdateCheckerService;
import enterprises.iwakura.docs.service.ValidatorService;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class Docs {

    public static final String HSTATS_MOD_ID = "aef85708-98dd-42f4-b42a-0d49e3576b29";

    private final List<BaseGlobalListener<?>> globalListeners;

    private final DocsCommand docsCommand;
    private final ReloadCommand reloadCommand;

    private final VoileAPI voileAPI;
    private final ConfigurationService configurationService;
    private final DocumentationService documentationService;
    private final ValidatorService validatorService;
    private final PluginAssetLoaderService pluginAssetLoaderService;
    private final UpdateCheckerService updateCheckerService;
    private final ImageService imageService;
    private final RuntimeImageAssetService runtimeImageAssetService;
    private final ServerService serverService;

    private final DocsPlugin plugin;
    private final Logger logger;

    public void setup() {
        logger.info("Initializing Voile...");
        logger.info("Made by Iwakura Enterprises");

        configurationService.init();
        validatorService.init(plugin.getDataDirectory());
        updateCheckerService.init();
        imageService.init();

        if (!serverService.isRunningOnDedicatedServer()) {
            logger.info("We're running in singleplayer! Disabling OOBE...");
            configurationService.getDocsConfig().setOutOfBoxExperience(false);
        }

        logger.info("Registering commands...");
        registerCommandShortcuts();
        plugin.getCommandRegistry().registerCommand(docsCommand);
        plugin.getCommandRegistry().registerCommand(reloadCommand);

        logger.info("Registering global listeners...");
        globalListeners.forEach(this::registerGlobalListener);

        logger.info("Initializing VoileAPI...");
        voileAPI.init();

        logger.info("Registering documentation loaders...");
        documentationService.registerDocumentationLoaders();

        new HStats(HSTATS_MOD_ID, plugin.getManifest().getVersion().toString());
    }

    /**
     * Registers command shortcuts
     */
    private void registerCommandShortcuts() {
        var commandShortcutsConfig = configurationService.getDocsConfig().getCommandShortcuts();

        if (commandShortcutsConfig.isEnabled()) {
            if (!commandShortcutsConfig.isOverrideHytaleCommands()) {
                docsCommand.initAliases();
            } else {
                logger.warn("Command shortcuts' Hytale command override is enabled. Voile will be able to override Hytale's default commands. These commands will have their own permission nodes.");
                logger.info("Found %d command shortcuts for /voile, registering them as standalone commands...".formatted(commandShortcutsConfig.getCommands().size()));
                for (Command command : configurationService.getDocsConfig().getCommandShortcuts().getCommands()) {
                    logger.info("[COMMAND-SHORTCUT] /%s -> topic %s (iwakuraenterprises.voile.command.%s)".formatted(
                        command.getName(), command.getTopicIdentifier(), command.getName()
                    ));
                    plugin.getCommandRegistry().registerCommand(new CommandShortcutCommand(command.getName(), docsCommand));
                }
            }
        } else {
            logger.warn("Command shortcuts are disabled, /voile will not have any aliases. You must restart the server if you have enabled the feature.");
        }
    }

    public void start() {
        runtimeImageAssetService.init();

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
