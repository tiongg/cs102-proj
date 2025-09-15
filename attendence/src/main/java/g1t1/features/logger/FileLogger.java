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
            String fileName = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fullFilePath = folderPath + fileName + ".log";
            File newFile = new File(fullFilePath);
            if (newFile.createNewFile()) {
                this.currentLogPath = fullFilePath;
            } else {
                System.out.println("Error creating file at " + folderPath + "!");
            }
        } catch (Exception e) {
            System.out.println("Error creating file at " + folderPath + "!");
        }
    }

    @Override
    public void log(LogLevel level, String message) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        String currentDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        BufferedWriter writer = new BufferedWriter(new FileWriter(this.currentLogPath, true));
        // Info and error should have additional padding
        // so that messages are aligned with the rest
        boolean hasAdditionalPadding = level == LogLevel.Info || level == LogLevel.Error;
        String padding = "\t".repeat(hasAdditionalPadding ? 2 : 1);

        try (writer) {
            writer.write(String.format("[%s]\t(%s)%s\t%s\n", currentDateTime, level, padding, message));
        }
    }
}
