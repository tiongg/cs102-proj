package g1t1.db.attendance;

import org.jooq.impl.DSL;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.Field;

import java.time.LocalDateTime;
import java.sql.Timestamp;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class AttendanceRepositoryJooq implements AttendanceRepository {
    private final DSLContext dsl;

    private final Table<?> ATTENDANCE_TABLE = DSL.table("attendance");
    private final Field<String> SESSION_ID = DSL.field("session_id", String.class);
    private final Field<String> ENROLLMENT_ID = DSL.field("enrollment_id", String.class);
    private final Field<String> STATUS = DSL.field("status", String.class);
    private final Field<LocalDateTime> RECORDED_TIMESTAMP = DSL.field("recorded_timestamp", java.time.LocalDateTime.class);
    
    public AttendanceRepositoryJooq(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public boolean create(String sessionId, String enrollmentId, String status) {
       return dsl.insertInto(ATTENDANCE_TABLE)
            .set(SESSION_ID, sessionId)
            .set(ENROLLMENT_ID, enrollmentId)
            .set(STATUS, status)
            .set(RECORDED_TIMESTAMP, LocalDateTime.now())
            .execute() > 0;
    }

    @Override
    public List<Attendance> fetchAttendenceBySessionId(String sessionId) {
        return dsl.select(SESSION_ID, ENROLLMENT_ID, STATUS, RECORDED_TIMESTAMP)
            .from(ATTENDANCE_TABLE)
            .where(SESSION_ID.eq(sessionId))
            .fetch(record -> new Attendance(
                record.get(SESSION_ID),
                record.get(ENROLLMENT_ID),
                record.get(STATUS),
                record.get(RECORDED_TIMESTAMP)
            ));
    }

    @Override
    public List<Attendance> fetchAttendenceByEnrollmentId(String enrollmentId) {
        return dsl.select(SESSION_ID, ENROLLMENT_ID, STATUS, RECORDED_TIMESTAMP)
            .from(ATTENDANCE_TABLE)
            .where(ENROLLMENT_ID.eq(enrollmentId))
            .fetch(record -> new Attendance(
                record.get(SESSION_ID),
                record.get(ENROLLMENT_ID),
                record.get(STATUS),
                record.get(RECORDED_TIMESTAMP)
            ));
    }

    @Override
    public boolean update(String sessionId, String enrollmentId, String statusNullable, Timestamp recordedTimestampNullable) {
        Map<Field<?>, Object> updateMap = new HashMap<>();
        if (statusNullable != null) {
            updateMap.put(STATUS, statusNullable);
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
