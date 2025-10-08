package g1t1.models.sessions;

import g1t1.db.attendance.AttendanceStatus;
import g1t1.db.attendance.MarkingMethod;
import g1t1.models.users.Student;

public class SessionAttendance {
    private final Student student;
    private AttendanceStatus status = AttendanceStatus.PENDING;
    private MarkingMethod method = MarkingMethod.MANUAL;
    private double confidence = -1d;

    public SessionAttendance(Student student) {
        this.student = student;
    }

    public AttendanceStatus getStatus() {
        return this.status;
    }

    public void markPresent(double confidence, MarkingMethod method) {
        this.status = AttendanceStatus.PRESENT;
        this.confidence = confidence;
        this.method = method;
    }

    public void markLate(double confidence, MarkingMethod method) {
        this.status = AttendanceStatus.LATE;
        this.confidence = confidence;
        this.method = method;
    }

    public void excuseStudent() {
        this.status = AttendanceStatus.EXCUSED;
        this.confidence = -1;
        this.method = MarkingMethod.MANUAL;
    }
}
