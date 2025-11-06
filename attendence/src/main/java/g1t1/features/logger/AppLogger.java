package g1t1.features.logger;

import g1t1.config.SettingsManager;
// TODO: Logging doesn't seem to actually work, need to re-check
/**
 * Static logging class.
 * 
 * Usage:
 * 
 * <pre>
 * {@code
 * Logger.log("This message is logged as an Info!");
 * }
 * </pre>
 */
public class AppLogger {
    private static BaseLogger logger;
    private static ConsoleLogger consoleLogger = new ConsoleLogger();

    // Initialize logger lazily to ensure SettingsManager is ready
    private static BaseLogger getLogger() {
        if (logger == null) {
            String logPath = SettingsManager.getInstance().getLogPath();
            logger = new FileLogger(logPath);
        }
        return logger;
    }

    /**
     * Logs message. Defaults to LogLevel.Info.
     * 
     * @param message - Message to log
     */
    public static void log(String message) {
        try {
            consoleLogger.log(message);
            getLogger().log(message);
        } catch (Exception e) {
            consoleLogger.log(LogLevel.Error, e.getMessage());
        }
    }

    /**
     * Logs message with the given log level.
     *
     * @param level   - Log level
     * @param message - Message to log
     */
    public static void log(LogLevel level, String message) {
        try {
            consoleLogger.log(level, message);
            getLogger().log(level, message);
        } catch (Exception e) {
            consoleLogger.log(LogLevel.Error, e.getMessage());
        }
    }

    /**
     * Logs a formatted message. Defaults to LogLevel.Info.
     *
     * @param format - Format string (e.g., "User %s logged in at %s")
     * @param args   - Arguments referenced by format specifiers
     */
    public static void logf(String format, Object... args) {
        log(String.format(format, args));
    }

    /**
     * Logs a formatted message with the given log level.
     *
     * @param level  - Log level
     * @param format - Format string (e.g., "User %s logged in at %s")
     * @param args   - Arguments referenced by format specifiers
     */
    public static void logf(LogLevel level, String format, Object... args) {
        log(level, String.format(format, args));
    }
}
