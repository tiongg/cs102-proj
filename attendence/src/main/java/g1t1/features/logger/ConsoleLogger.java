package g1t1.features.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConsoleLogger extends BaseLogger {
    @Override
    public void log(LogLevel level, String message) {
        LocalDateTime now = LocalDateTime.now();
        String currentDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // Info and error should have additional padding
        // so that messages are aligned with the rest
        boolean hasAdditionalPadding = level == LogLevel.Info || level == LogLevel.Error;
        String padding = "\t".repeat(hasAdditionalPadding ? 2 : 1);

        String formattedMessage = String.format("[%s]\t(%s)%s%s", colorString(ConsoleColors.CYAN, currentDateTime),
                colorString(getColorCode(level), level.toString()), padding, message);

        System.out.println(formattedMessage);
    }

    private String getColorCode(LogLevel level) {
        switch (level) {
        case Error:
            return ConsoleColors.RED_BRIGHT;
        case Info:
            return ConsoleColors.BLUE_BRIGHT;
        case Success:
            return ConsoleColors.GREEN_BRIGHT;
        case Warning:
            return ConsoleColors.YELLOW_BRIGHT;
        default:
            return ConsoleColors.WHITE;
        }
    }

    private String colorString(String color, String message) {
        return String.format("%s%s%s", color, message, ConsoleColors.RESET);
    }
}
