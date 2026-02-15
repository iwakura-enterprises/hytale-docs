package enterprises.iwakura.docs.util;

import com.hypixel.hytale.logger.HytaleLogger;

import lombok.Getter;

@Getter
public class Logger {

    private final HytaleLogger logger;

    public Logger(String name) {
        this.logger = HytaleLogger.get(name);
    }

    public void info(String message) {
        logger.atInfo().log(message);
    }

    public void warn(String message) {
        logger.atWarning().log(message);
    }

    public void error(String message) {
        logger.atSevere().log(message);
    }

    public void error(String message, Throwable throwable) {
        logger.atSevere().withCause(throwable).log(message + ": " + throwable.getMessage());
    }
}
