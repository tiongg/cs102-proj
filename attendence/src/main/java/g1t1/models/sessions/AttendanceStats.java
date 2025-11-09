package g1t1.models.sessions;

public record AttendanceStats(int present, int expected, int total) {
    public double percent() {
        if (expected() <= 0) {
            return 0;
        }
        return (((double) present() / expected()) * 100);
    }
}
