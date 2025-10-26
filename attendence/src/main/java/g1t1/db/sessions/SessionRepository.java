package g1t1.db.sessions;

import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.SessionStatus;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface SessionRepository {
    String create(String moduleSectionId, Date date, short week, Timestamp startTime, Timestamp endTimeNullable, SessionStatus status);

    String create(ClassSession session);

    Optional<SessionRecord> fetchSessionById(String sessionId);

    List<SessionRecord> fetchSessionsByModuleSectionId(String moduleSectionId);

    List<SessionRecord> fetchSessionsByTeacherUserIdAndWeek(String teacherUserId, short week);

    boolean update(String sessionId, String moduleSectionIdNullable, Date dateNullable, Integer weekNullable, Timestamp startTimeNullable, Timestamp endTimeNullable, String statusNullable);

    boolean deleteSessionById(String sessionId);

    boolean deleteSessionsByModuleSectionId(String moduleSectionId);
}
