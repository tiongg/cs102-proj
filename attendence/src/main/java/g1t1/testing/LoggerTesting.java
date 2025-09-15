package g1t1.testing;

import g1t1.features.logger.AppLogger;
import g1t1.features.logger.LogLevel;

// Just testing logging stuff
public class LoggerTesting {
    public static void main(String[] args) {
        AppLogger.log(LogLevel.Success, "Application started!");
        AppLogger.log(LogLevel.Info, "This is an info");
        AppLogger.log(LogLevel.Warning, "This is a warning");
        AppLogger.log(LogLevel.Error, "We goofed up");
    }
}
