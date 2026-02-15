package enterprises.iwakura.docs.service;

import java.nio.file.Files;
import java.nio.file.Path;

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
        return jean.getOrLoad("config", DocsConfig.class);
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
        getDocsConfig();
    }
}
