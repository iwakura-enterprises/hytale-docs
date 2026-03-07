package enterprises.iwakura.docs.util;

import com.hypixel.hytale.logger.HytaleLogger;

import enterprises.iwakura.docs.service.SentryService;
import io.sentry.SentryLevel;
import lombok.Getter;

@Getter
public class Logger {

    private final HytaleLogger logger;

    public Logger(String name) {
        this.logger = HytaleLogger.get(name);
    }

    public void info(String message) {
        logger.atInfo().log(message);
        SentryService.addBreadcrumb(message, SentryLevel.INFO);
    }

    public void warn(String message) {
        logger.atWarning().log(message);
        SentryService.addBreadcrumb(message, SentryLevel.WARNING);
    }

    public void error(String message) {
        logger.atSevere().log(message);
        SentryService.addBreadcrumb(message, SentryLevel.ERROR);
    }

    public void error(String message, Throwable throwable) {
        error(message, throwable, true);
    }

    public void error(String message, Throwable throwable, boolean sentry) {
        logger.atSevere().withCause(throwable).log(message + ": " + throwable.getMessage());

        if (sentry) {
            SentryService.addBreadcrumb(message, SentryLevel.ERROR);
            SentryService.captureException(throwable);
        }
    }
}
