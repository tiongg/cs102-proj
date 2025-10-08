package g1t1.db.attendance;

import java.sql.Timestamp;
import java.util.List;

public interface AttendanceRepository {
    boolean create(String sessionId, String enrollmentId, AttendanceStatus status, double confidence, MarkingMethod method);

    List<Attendance> fetchAttendenceBySessionId(String sessionId);

    List<Attendance> fetchAttendenceByEnrollmentId(String enrollmentId);

    boolean update(String sessionIdNullable, String enrollmentIdNullable, AttendanceStatus statusNullable, Timestamp recordedTimestampNullable);

    boolean delete(String sessionId, String enrollmentId);
}
