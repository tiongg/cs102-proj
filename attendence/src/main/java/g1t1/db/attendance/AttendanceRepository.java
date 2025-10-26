package g1t1.db.attendance;

import g1t1.models.sessions.ClassSession;

import java.sql.Timestamp;
import java.util.List;

public interface AttendanceRepository {
    boolean create(String sessionId, String enrollmentId, AttendanceStatus status, double confidence, MarkingMethod method);

    boolean createAll(String sessionId, ClassSession session);

    List<AttendanceRecord> fetchAttendenceBySessionId(String sessionId);

    List<AttendanceRecord> fetchAttendenceByEnrollmentId(String enrollmentId);

    boolean update(String sessionIdNullable, String enrollmentIdNullable, AttendanceStatus statusNullable, Timestamp recordedTimestampNullable);

    boolean delete(String sessionId, String enrollmentId);
}
