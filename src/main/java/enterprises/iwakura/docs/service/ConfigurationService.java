package enterprises.iwakura.docs.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import com.google.gson.Gson;

import enterprises.iwakura.docs.DocsPlugin;
import enterprises.iwakura.docs.config.DocsConfig;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.jean.Jean;
import enterprises.iwakura.jean.LoadOptions;
import enterprises.iwakura.jean.serializer.GsonSerializer;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Bean
@RequiredArgsConstructor
public class ConfigurationService {

    private final Logger logger;
    private final Gson gson;
    private final DocsPlugin plugin;
    private Jean jean;

    /**
     * Initializes the ConfigurationService in the specified directory
     */
    public void init() {
        logger.info("Initializing ConfigurationService in directory: %s".formatted(plugin.getDataDirectory()));
        jean = new Jean(
            plugin.getDataDirectory(),
            new GsonSerializer(gson),
            LoadOptions.builder().saveOnLoad(true).build()
        );

        reload();
    }

    /**
     * Returns loaded {@link DocsConfig}
     *
     * @return {@link DocsConfig}
     */
    public DocsConfig getDocsConfig() {
        // TODO: Somehow validate config and if invalid, reset so players in singleplayer don't have to delete the config
        return jean.getOrLoad("config", DocsConfig.class);
    }

    /**
     * Saves {@link DocsConfig} to the file system
     *
     * @param docsConfig Docs config
     */
    public void saveDocsConfig(DocsConfig docsConfig) {
        jean.save("config", docsConfig);
    }

    /**
     * Clears configuration cache
     */
    @SneakyThrows
    public void reload() {
        if (Files.exists(plugin.getDataDirectory())) {
            Files.createDirectories(plugin.getDataDirectory());
        }

        jean.clearCache();
        var config = getDocsConfig();
        config.getFileSystemCache().ensureAllTypes();

        if (config.getInterfacePreferencesDefaults().getChecksum() == null) {
            logger.warn("Null checksum for interface preferences defaults! Setting a new one...");
            resetInterfacePreferencesDefaultsChecksum();
        }

        saveDocsConfig(config);
    }

    /**
     * Resets interface preferences defaults' checksum
     */
    public void resetInterfacePreferencesDefaultsChecksum() {
        logger.warn("Resetting interface preferences defaults' checksum! All players will have their interface preferences reset.");
        var config = getDocsConfig();
        config.getInterfacePreferencesDefaults().setChecksum(UUID.randomUUID());
        saveDocsConfig(config);
    }
}
