package g1t1.db.attendance;

import g1t1.db.enrollments.Enrollment;
import g1t1.db.enrollments.EnrollmentRepository;
import g1t1.db.enrollments.EnrollmentRepositoryJooq;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.SessionAttendance;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceRepositoryJooq implements AttendanceRepository {
    private final DSLContext dsl;

    private final Table<?> ATTENDANCE_TABLE = DSL.table("attendance");
    private final Field<String> SESSION_ID = DSL.field("session_id", String.class);
    private final Field<String> ENROLLMENT_ID = DSL.field("enrollment_id", String.class);
    private final Field<String> STATUS = DSL.field("status", String.class);
    private final Field<Double> CONFIDENCE = DSL.field("confidence", Double.class);
    private final Field<String> METHOD = DSL.field("method", String.class);
    private final Field<LocalDateTime> RECORDED_TIMESTAMP = DSL.field("recorded_timestamp", java.time.LocalDateTime.class);

    public AttendanceRepositoryJooq(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public boolean create(String sessionId, String enrollmentId, AttendanceStatus status, double confidence, MarkingMethod method) {
        return dsl.insertInto(ATTENDANCE_TABLE)
                .set(SESSION_ID, sessionId)
                .set(ENROLLMENT_ID, enrollmentId)
                .set(STATUS, status.toString().toLowerCase())
                .set(CONFIDENCE, confidence)
                .set(METHOD, method.toString())
                .set(RECORDED_TIMESTAMP, LocalDateTime.now())
                .execute() > 0;
    }

    @Override
    public boolean createAll(String sessionId, ClassSession session) {
        EnrollmentRepository enrollmentRepository = new EnrollmentRepositoryJooq(this.dsl);

        Map<String, String> studentIdToEnrollmentId = new HashMap<>();
        for (Enrollment enrollment : enrollmentRepository.fetchEnrollmentsByModuleSectionId(session.getModuleSection().getId())) {
            studentIdToEnrollmentId.put(enrollment.studentId(), enrollment.enrollmentId());
        }

        for (SessionAttendance attendance : session.getStudentAttendance().values()) {
            create(
                    sessionId,
                    studentIdToEnrollmentId.get(attendance.getStudent().getId().toString()),
                    attendance.getStatus(),
                    attendance.getConfidence(),
                    attendance.getMethod()
            );
        }

        return true;
    }

    @Override
    public List<AttendanceRecord> fetchAttendenceBySessionId(String sessionId) {
        return dsl.select(SESSION_ID, ENROLLMENT_ID, CONFIDENCE, STATUS, RECORDED_TIMESTAMP, METHOD)
                .from(ATTENDANCE_TABLE)
                .where(SESSION_ID.eq(sessionId))
                .fetch(record -> new AttendanceRecord(
                        record.get(SESSION_ID),
                        record.get(ENROLLMENT_ID),
                        record.get(STATUS),
                        record.get(CONFIDENCE),
                        record.get(METHOD),
                        record.get(RECORDED_TIMESTAMP)
                ));
    }

    @Override
    public List<AttendanceRecord> fetchAttendenceByEnrollmentId(String enrollmentId) {
        return dsl.select(SESSION_ID, ENROLLMENT_ID, STATUS, RECORDED_TIMESTAMP)
                .from(ATTENDANCE_TABLE)
                .where(ENROLLMENT_ID.eq(enrollmentId))
                .fetch(record -> new AttendanceRecord(
                        record.get(SESSION_ID),
                        record.get(ENROLLMENT_ID),
                        record.get(STATUS),
                        record.get(CONFIDENCE),
                        record.get(METHOD),
                        record.get(RECORDED_TIMESTAMP)
                ));
    }

    @Override
    public boolean update(String sessionId, String enrollmentId, AttendanceStatus statusNullable, Timestamp recordedTimestampNullable) {
        Map<Field<?>, Object> updateMap = new HashMap<>();
        if (statusNullable != null) {
            updateMap.put(STATUS, statusNullable.toString());
        }
        if (updateMap.isEmpty()) {
            return false;
        }
        if (recordedTimestampNullable != null) {
            updateMap.put(RECORDED_TIMESTAMP, recordedTimestampNullable.toLocalDateTime());
        }
        return dsl.update(ATTENDANCE_TABLE)
                .set(updateMap)
                .where(SESSION_ID.eq(sessionId).and(ENROLLMENT_ID.eq(enrollmentId)))
                .execute() > 0;
    }

    public boolean delete(String sessionId, String enrollmentId) {
        return dsl.deleteFrom(ATTENDANCE_TABLE)
                .where(SESSION_ID.eq(sessionId).and(ENROLLMENT_ID.eq(enrollmentId)))
                .execute() > 0;
    }
}
