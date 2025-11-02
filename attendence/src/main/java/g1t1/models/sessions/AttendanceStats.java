package g1t1.models.sessions;

public record AttendanceStats(int present, int expected, int total) {
    public double percent() {
        return (((double) present() / expected()) * 100);
    }
}
