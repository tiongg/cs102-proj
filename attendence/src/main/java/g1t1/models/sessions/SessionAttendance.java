package g1t1.models.sessions;

import g1t1.db.attendance.AttendanceStatus;
import g1t1.db.attendance.MarkingMethod;
import g1t1.models.users.Student;

import java.time.LocalDateTime;

public class SessionAttendance {
    private final Student student;
    private AttendanceStatus status = AttendanceStatus.PENDING;
    private MarkingMethod method = MarkingMethod.MANUAL;
    // Scale of 0-100
    private double confidence = -100d;
    private LocalDateTime lastUpdated = LocalDateTime.now();

    public SessionAttendance(Student student) {
        this.student = student;
    }

    public AttendanceStatus getStatus() {
        return this.status;
    }

    public Student getStudent() {
        return this.student;
    }

    public double getConfidence() {
        return this.confidence;
    }

    public MarkingMethod getMethod() {
        return this.method;
    }

    public LocalDateTime getLastUpdated() {
        return this.lastUpdated;
    }

    public void setStatus(AttendanceStatus status, double confidence, MarkingMethod method) {
        this.status = status;
        this.confidence = confidence;
        this.method = method;
        this.lastUpdated = LocalDateTime.now();
    }

    public void updateBestConfidence(double confidence) {
        this.confidence = Math.max(confidence, this.confidence);
    }
}
