package g1t1.db.sessions;

import java.sql.Date;
import java.sql.Timestamp;

import java.util.List;
import java.util.Optional;

public interface SessionRepository {
    String create(String moduleSectionId, Date date, short week, Timestamp startTime, Timestamp endTimeNullable, String status);
    Optional<Session> fetchSessionById(String sessionId);
    List<Session> fetchSessionsByModuleSectionId(String moduleSectionId);
    List<Session> fetchSessionsByTeacherUserIdAndWeek(String teacherUserId, short week);
    boolean update(String sessionId, String moduleSectionIdNullable, Date dateNullable, Integer weekNullable, Timestamp startTimeNullable, Timestamp endTimeNullable, String statusNullable);
    boolean deleteSessionById(String sessionId);
    boolean deleteSessionsByModuleSectionId(String moduleSectionId);
}
