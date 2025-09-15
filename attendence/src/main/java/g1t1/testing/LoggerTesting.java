package g1t1.testing;

import g1t1.features.logger.LogLevel;
import g1t1.features.logger.Logger;

// Just testing logging stuff
public class LoggerTesting {
    public static void main(String[] args) {
        Logger.log(LogLevel.Success, "Application started!");
        Logger.log(LogLevel.Info, "This is an info");
        Logger.log(LogLevel.Warning, "This is a warning");
        Logger.log(LogLevel.Error, "We goofed up");
    }
}
