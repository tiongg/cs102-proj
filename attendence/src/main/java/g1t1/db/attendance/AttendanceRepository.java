package g1t1.db.attendance;

import java.util.List;
import java.sql.Timestamp;

public interface AttendanceRepository {
    boolean create(String sessionId, String enrollmentId, String status);
    List<Attendance> fetchAttendenceBySessionId(String sessionId);
    List<Attendance> fetchAttendenceByEnrollmentId(String enrollmentId);
    boolean update(String sessionIdNullable, String enrollmentIdNullable, String statusNullable, Timestamp recordedTimestampNullable);
    boolean delete(String sessionId, String enrollmentId);
}
