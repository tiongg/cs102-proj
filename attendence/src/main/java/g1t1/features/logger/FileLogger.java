package g1t1.features.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileLogger extends BaseLogger {
    private String currentLogPath;

    /**
     * Creates a new file logger, creating logs at folderPath
     * 
     * @param folderPath - Path to folder to store logs
     */
    public FileLogger(String folderPath) {
        LocalDateTime now = LocalDateTime.now();
        try {
            // Ensure folder path ends with separator
            String normalizedPath = folderPath;
            if (!normalizedPath.endsWith(File.separator)) {
                normalizedPath += File.separator;
            }

            // Create logs directory if it doesn't exist
            File logsDir = new File(folderPath);
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }

            String fileName = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fullFilePath = normalizedPath + fileName + ".log";
            File newFile = new File(fullFilePath);
            if (newFile.createNewFile()) {
                this.currentLogPath = fullFilePath;
            } else {
                // File already exists, use it
                this.currentLogPath = fullFilePath;
            }
        } catch (Exception e) {
            System.out.println("Error creating file at " + folderPath + "! Exception: " + e.getMessage());
            // Fallback: Use a temporary logs file if all else fails
            try {
                File tempDir = new File(System.getProperty("java.io.tmpdir"));
                this.currentLogPath = tempDir.getAbsolutePath() + File.separator + "app_logs.txt";
            } catch (Exception fallbackException) {
                System.out.println("Critical error: Cannot create any log file!");
            }
        }
    }

    @Override
    public void log(LogLevel level, String message) throws Exception {
        // Safety check: if currentLogPath is null, we cannot log to file
        if (this.currentLogPath == null) {
            throw new IllegalStateException("Log file path is not initialized. Cannot write logs to file.");
        }

        LocalDateTime now = LocalDateTime.now();
        String currentDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Info and error should have additional padding
        // so that messages are aligned with the rest
        boolean hasAdditionalPadding = level == LogLevel.Info || level == LogLevel.Error;
        String padding = "\t".repeat(hasAdditionalPadding ? 2 : 1);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.currentLogPath, true))) {
            writer.write(String.format("[%s]\t(%s)%s\t%s\n", currentDateTime, level, padding, message));
        }
    }
}
