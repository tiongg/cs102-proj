package g1t1.utils;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class DateUtils {
    public static final String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    public static String dayOfWeekToWord(int dayOfWeek) {
        return daysOfWeek[dayOfWeek];
    }

    public static Date toSqlDate(LocalDateTime date) {
        return Date.valueOf(date.toLocalDate());
    }

    public static Timestamp toTimestamp(LocalDateTime date) {
        return Timestamp.valueOf(date);
    }

    public static LocalDateTime timestampToLocalDateTime(Timestamp ts) {
        return ts.toLocalDateTime();
    }

    public static LocalDateTime dateToLocalDatetime(Date date) {
        return date.toLocalDate().atStartOfDay();
    }
}
