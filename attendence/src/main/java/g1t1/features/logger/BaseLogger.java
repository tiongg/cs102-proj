package g1t1.features.logger;

public abstract class BaseLogger {
    public void log(String message) throws Exception {
        this.log(LogLevel.Info, message);
    }

    public abstract void log(LogLevel level, String message) throws Exception;
}
