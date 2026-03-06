package enterprises.iwakura.docs.service;

import java.util.Optional;

import com.hypixel.hytale.server.core.universe.Universe;

import enterprises.iwakura.docs.Version;
import enterprises.iwakura.docs.object.CacheIndex;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import io.sentry.ISentryClient;
import io.sentry.SentryClient;
import io.sentry.SentryOptions;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class SentryService {

    public static final String DEFAULT_DSN = "https://3c85b34fde4f402694826c65213c0dc4@glitchtip.iwakura.enterprises/4";

    private final FileSystemCacheService fileSystemCacheService;
    private final ServerService serverService;
    private final ConfigurationService configurationService;
    private final Logger logger;

    private static ISentryClient sentryClient;

    /**
     * Initializes SentryService
     */
    public void init() {
        var config = configurationService.getDocsConfig().getSentry();

        if (!config.isEnabled()) {
            logger.warn("Voile sentry is disabled.");
            return;
        }

        var dsn = Optional.ofNullable(config.getDsnOverride()).orElse(DEFAULT_DSN);

        if (!dsn.equals(DEFAULT_DSN)) {
            logger.warn("Using DSN override for Voile sentry: " + dsn);
        }

        var options = new SentryOptions();
        options.setDsn(dsn);
        options.setEnableExternalConfiguration(false);
        options.setDebug(true);
        options.setFlushTimeoutMillis(10);

        options.setBeforeSend((event, hint) -> {
            try {
                event.setTag("java.version", System.getProperty("java.version"));
                event.setTag("java.vendor", System.getProperty("java.vendor"));
                event.setTag("os.name", System.getProperty("os.name"));
                event.setTag("os.arch", System.getProperty("os.arch"));
                event.setTag("hytale.server-id", String.valueOf(config.getServerId()));
                event.setTag("hytale.dedicated-server", String.valueOf(serverService.isRunningOnDedicatedServer()));

                event.setExtra("hytale.online-players", String.valueOf(Universe.get().getPlayerCount()));
                event.setExtra("voile.file-system-cache.size", String.valueOf(Optional.ofNullable(fileSystemCacheService.getCacheIndex()).map(CacheIndex::size).orElse(0)));
                event.setExtra("voile.runtime-image-asset.size", String.valueOf(RuntimeImageAssetService.getRUNTIME_IMAGE_ASSET_MAP().size()));
                event.setExtra("voile.runtime-image-asset.players", String.valueOf(RuntimeImageAssetService.getPLAYER_RUNTIME_IMAGE_ASSETS_MAP().size()));
            } catch (Exception exception) {
                logger.error("Failed to create extra data for sentry", exception, false);
            }
            return event;
        });

        options.setRelease("voile@" + Version.VERSION);
        options.setEnvironment("production");

        sentryClient = new SentryClient(options);
        logger.info("Voile sentry initialized.");
    }

    public static void captureException(Throwable throwable) {
        if (sentryClient != null) {
            sentryClient.captureException(throwable);
        }
    }
}
