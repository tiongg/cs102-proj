package g1t1.features.logger;

public enum LogLevel {
    /**
     * Default log level. Logged for information/diagnostics sake
     */
    Info,

    /**
     * Warning level. Things might go wrong if it continues.
     */
    Warning,

    /**
     * Error has been thrown in the application
     */
    Error,

    /**
     * Successful at something. Happy log!!
     */
    Success
}
