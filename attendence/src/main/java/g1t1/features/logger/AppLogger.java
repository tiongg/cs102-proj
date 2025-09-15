package g1t1.features.logger;

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
    // TODO(TG/Delroy): File logger path should come from settings
    private static BaseLogger logger = new FileLogger("./logs/");
    private static ConsoleLogger consoleLogger = new ConsoleLogger();

    /**
     * Logs message. Defaults to LogLevel.Info.
     * 
     * @param message - Message to log
     */
    public static void log(String message) {
        try {
            consoleLogger.log(message);
            logger.log(message);
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
            logger.log(level, message);
        } catch (Exception e) {
            consoleLogger.log(LogLevel.Error, e.getMessage());
        }
    }
}
