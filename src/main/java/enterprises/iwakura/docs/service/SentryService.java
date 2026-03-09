package enterprises.iwakura.docs.service;

import java.net.ConnectException;
import java.net.SocketException;
import java.nio.channels.UnresolvedAddressException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.net.ssl.SSLHandshakeException;

import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.Universe;

import enterprises.iwakura.docs.Version;
import enterprises.iwakura.docs.object.CacheIndex;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import io.sentry.Attachment;
import io.sentry.Breadcrumb;
import io.sentry.IScope;
import io.sentry.ISentryClient;
import io.sentry.Scope;
import io.sentry.SentryClient;
import io.sentry.SentryLevel;
import io.sentry.SentryOptions;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class SentryService {

    public static final String DEFAULT_DSN = "https://3c85b34fde4f402694826c65213c0dc4@glitchtip.iwakura.enterprises/4";

    private static final Map<String, String> installedPluginsMap = new HashMap<>();
    private static final List<Class<? extends Throwable>> ignoredExceptions = List.of(
        SSLHandshakeException.class,
        UnresolvedAddressException.class,
        ConnectException.class,
        SocketException.class
    );

    private final FileSystemCacheService fileSystemCacheService;
    private final ServerService serverService;
    private final ConfigurationService configurationService;
    private final Logger logger;

    private static ISentryClient sentryClient;
    private static IScope sentryScope;

    /**
     * Initializes SentryService
     */
    public void init() {
        var config = configurationService.getDocsConfig().getSentry();

        if (!config.isEnabled()) {
            logger.warn("Voile sentry is disabled.");
            return;
        }

        installedPluginsMap.putAll(PluginManager.get().getPlugins().stream()
            .filter(plugin -> !plugin.getIdentifier().getGroup().equals("Hytale"))
            .collect(Collectors.toMap(
                k -> k.getIdentifier().toString(),
                v -> v.getManifest().getVersion().toString()
            )));

        var dsn = Optional.ofNullable(config.getDsnOverride()).orElse(DEFAULT_DSN);

        if (!dsn.equals(DEFAULT_DSN)) {
            logger.warn("Using DSN override for Voile sentry: " + dsn);
        }

        var options = new SentryOptions();
        options.setDsn(dsn);
        options.setEnableExternalConfiguration(false);
        options.setMaxBreadcrumbs(50);
        options.setAttachStacktrace(true);
        options.setAttachThreads(false);
        options.setSendDefaultPii(false); // Don't send personally identifiable info
        options.setMaxQueueSize(30);
        options.setFlushTimeoutMillis(2000);
        options.setEnableShutdownHook(true);
        options.setShutdownTimeoutMillis(2000);
        options.setServerName(String.valueOf(config.getServerId()));

        options.setBeforeSend((event, hint) -> {
            try {
                // Ignore specific exceptions
                if (event.getThrowable() != null) {
                    boolean shouldIgnore = ignoredExceptions.stream()
                        .anyMatch(ignoredExceptionClass ->
                            event.getThrowable().getClass().isAssignableFrom(ignoredExceptionClass));
                    if (shouldIgnore) {
                        return null;
                    }
                }

                event.setTag("java.version", System.getProperty("java.version"));
                event.setTag("java.vendor", System.getProperty("java.vendor"));
                event.setTag("os.name", System.getProperty("os.name"));
                event.setTag("os.arch", System.getProperty("os.arch"));
                event.setTag("hytale.dedicated-server", String.valueOf(serverService.isRunningOnDedicatedServer()));
                event.setTag("hytale.version", ManifestUtil.getVersion());

                event.setExtra("hytale.online-players", Universe.get().getPlayerCount());
                event.setExtra("hytale.plugin-count", installedPluginsMap.size());
                event.setExtra("hytale.plugins", installedPluginsMap);
                event.setExtra("voile.file-system-cache.size", Optional.ofNullable(fileSystemCacheService.getCacheIndex()).map(CacheIndex::size).orElse(0));
                event.setExtra("voile.runtime-image-asset.size", RuntimeImageAssetService.getRUNTIME_IMAGE_ASSET_MAP().size());
                event.setExtra("voile.runtime-image-asset.players", RuntimeImageAssetService.getPLAYER_RUNTIME_IMAGE_ASSETS_MAP().size());
            } catch (Exception exception) {
                logger.error("Failed to create extra data for sentry", exception, false);
            }
            return event;
        });

        options.setRelease("voile@" + Version.VERSION);
        options.setEnvironment("production");

        sentryClient = new SentryClient(options);
        sentryScope = new Scope(options);
        logger.info("Voile sentry initialized.");
    }

    public static void addBreadcrumb(String message, SentryLevel level) {
        if (sentryScope != null) {
            Breadcrumb breadcrumb = new Breadcrumb(message);
            breadcrumb.setLevel(level);
            sentryScope.addBreadcrumb(breadcrumb);
        }
    }

    public static void addAttachment(String content, String fileName) {
        if (sentryScope != null) {
            sentryScope.setExtra(fileName, content);
        }
    }

    public static void captureException(Throwable throwable) {
        if (sentryClient != null) {
            sentryClient.captureException(throwable, sentryScope);
        }
    }

    public static void captureMessage(String message, SentryLevel level) {
        if (sentryClient != null) {
            sentryClient.captureMessage(message, level);
        }
    }
}
