package g1t1.utils;

public class DateUtils {
    public static final String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    public static String dayOfWeekToWord(int dayOfWeek) {
        return daysOfWeek[dayOfWeek];
    }
}
