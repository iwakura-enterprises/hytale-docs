package enterprises.iwakura.docs.service;

import java.util.Timer;
import java.util.TimerTask;

import enterprises.iwakura.docs.Version;
import enterprises.iwakura.docs.api.CurseForgeVersionCheckerApi;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Bean
@Getter
@RequiredArgsConstructor
public class UpdateCheckerService {

    private static final Timer timer = new Timer();

    private final ConfigurationService configurationService;
    private final CurseForgeVersionCheckerApi curseForgeVersionCheckerApi;
    private final Logger logger;

    private boolean updateAvailable = false;
    private String updateVersion = null;

    /**
     * Initializes update checker service
     */
    public void init() {
        var docsConfig = configurationService.getDocsConfig();

        if (!docsConfig.isUpdateCheckerEnabled()) {
            logger.warn("Update checker is disabled!");
        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    checkForUpdates();
                } catch (Exception exception) {
                    logger.error("Failed to check for updates!", exception);
                }
            }
        }, 0, 1000 * 60 * 60); // Every hour
    }

    private void checkForUpdates() {
        var docsConfig = configurationService.getDocsConfig();
        if (!docsConfig.isUpdateCheckerEnabled()) {
            return; // Update checker disabled
        }

        curseForgeVersionCheckerApi.fetch().send().whenCompleteAsync((response, exception) -> {
            if (exception != null) {
                logger.error("Failed to check for updates!", exception);
                return;
            }

            var latestVersion = response.getLatestVersionNumber();

            if (latestVersion.isPresent()) {
                checkIfNewerVersion(latestVersion.get());
            } else {
                logger.warn("Could not find latest version in " + response);
            }
        });
    }

    private void checkIfNewerVersion(String latestVersionNumber) {
        var currentVersion = Version.VERSION;

        if (isNewerVersion(latestVersionNumber, currentVersion)) {
            logger.warn("A new version is available: " + latestVersionNumber + " (current: " + currentVersion + ") (https://www.curseforge.com/hytale/mods/docs)");
            updateAvailable = true;
            updateVersion = latestVersionNumber;
        }
    }

    private boolean isNewerVersion(String latest, String current) {
        int[] latestParts = parseVersion(latest);
        int[] currentParts = parseVersion(current);

        for (int i = 0; i < 3; i++) {
            if (latestParts[i] > currentParts[i]) {
                return true;
            } else if (latestParts[i] < currentParts[i]) {
                return false;
            }
        }
        return false;
    }

    private int[] parseVersion(String version) {
        int[] parts = new int[3];
        String[] split = version.split("\\.");

        for (int i = 0; i < Math.min(split.length, 3); i++) {
            try {
                parts[i] = Integer.parseInt(split[i].trim());
            } catch (NumberFormatException e) {
                parts[i] = 0;
            }
        }
        return parts;
    }
}
