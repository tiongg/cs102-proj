package g1t1.db.sessions;

import org.jooq.impl.DSL;

import java.sql.Date;
import java.sql.Timestamp;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class SessionRepositoryJooq implements SessionRepository {
    private final DSLContext dsl;

    private final Table<?> SESSIONS_TABLE = DSL.table("sessions");
    private final Field<String> SESSION_ID = DSL.field("session_id", String.class);
    private final Field<String> MODULE_SECTION_ID = DSL.field("module_section_id", String.class);
    private final Field<Date> DATE = DSL.field("date", SQLDataType.DATE);
    private final Field<Short> WEEK = DSL.field("week", Short.class);
    private final Field<Timestamp> START_TIME = DSL.field("start_time", SQLDataType.TIMESTAMP);
    private final Field<Timestamp> END_TIME = DSL.field("end_time", SQLDataType.TIMESTAMP);
    private final Field<String> STATUS = DSL.field("status", String.class);
    private final Field<Timestamp> CREATED_AT = DSL.field("created_at", SQLDataType.TIMESTAMP);

    public SessionRepositoryJooq(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public String create(String moduleSectionId, Date date, short week, Timestamp startTime, Timestamp endTimeNullable, String status) {
        String uuid = UUID.randomUUID().toString();
        dsl.insertInto(SESSIONS_TABLE)
            .set(SESSION_ID, uuid)
            .set(MODULE_SECTION_ID, moduleSectionId)
            .set(DATE, date)
            .set(WEEK, week)
            .set(START_TIME, startTime)
            .set(END_TIME, endTimeNullable)
            .set(STATUS, status)
            .execute();
        return uuid;
    }

    @Override
    public Optional<Session> fetchSessionById(String sessionId) {
        return dsl.select(SESSION_ID, MODULE_SECTION_ID, DATE, WEEK, START_TIME, END_TIME, STATUS, CREATED_AT)
            .from(SESSIONS_TABLE)
            .where(SESSION_ID.eq(sessionId))
            .fetchOptional(record -> new Session(
                record.get(SESSION_ID),
                record.get(MODULE_SECTION_ID),
                record.get(DATE),
                record.get(WEEK),
                record.get(START_TIME),
                record.get(END_TIME),
                record.get(STATUS),
                record.get(CREATED_AT)
            ));
    }

    @Override 
    public List<Session> fetchSessionsByModuleSectionId(String moduleSectionId) {
        return dsl.selectFrom(SESSIONS_TABLE)
            .where(MODULE_SECTION_ID.eq(moduleSectionId))
            .fetch()
            .map(record -> new Session(
                record.get(SESSION_ID),
                record.get(MODULE_SECTION_ID),
                record.get(DATE),
                record.get(WEEK),
                record.get(START_TIME),
                record.get(END_TIME),
                record.get(STATUS),
                record.get(CREATED_AT)
            ));
    }

    @Override
    public List<Session> fetchSessionsByTeacherUserIdAndWeek(String teacherUserId, short week) {
        var s = DSL.table(DSL.name("sessions")).as("s");
        var m = DSL.table(DSL.name("module_sections")).as("m");

        // qualified session fields
        Field<String>     S_SESSION_ID        = DSL.field(DSL.name("s","session_id"),        String.class);
        Field<String>     S_MODULE_SECTION_ID = DSL.field(DSL.name("s","module_section_id"), String.class);
        Field<Date>       S_DATE              = DSL.field(DSL.name("s","date"),              java.sql.Date.class);
        Field<Short>      S_WEEK              = DSL.field(DSL.name("s","week"),              Short.class);
        Field<Timestamp>  S_START             = DSL.field(DSL.name("s","start_time"),        java.sql.Timestamp.class);
        Field<Timestamp>  S_END               = DSL.field(DSL.name("s","end_time"),          java.sql.Timestamp.class);
        Field<String>     S_STATUS            = DSL.field(DSL.name("s","status"),            String.class);
        Field<Timestamp>  S_CREATED           = DSL.field(DSL.name("s","created_at"),        java.sql.Timestamp.class);

        // qualified module_sections fields
        Field<String>     M_MODULE_SECTION_ID = DSL.field(DSL.name("m","module_section_id"), String.class);
        Field<String>     M_TEACHER_USER_ID   = DSL.field(DSL.name("m","teacher_user_id"),   String.class);

        return dsl.select(S_SESSION_ID, S_MODULE_SECTION_ID, S_DATE, S_WEEK, S_START, S_END, S_STATUS, S_CREATED)
                .from(s.join(m).on(S_MODULE_SECTION_ID.eq(M_MODULE_SECTION_ID)))
                .where(M_TEACHER_USER_ID.eq(teacherUserId))
                .and(S_WEEK.eq(week)) 
                .fetch(r -> new Session(
                    r.get(S_SESSION_ID),
                    r.get(S_MODULE_SECTION_ID),
                    r.get(S_DATE),
                    r.get(S_WEEK),
                    r.get(S_START),
                    r.get(S_END),
                    r.get(S_STATUS),
                    r.get(S_CREATED)
                ));
    }


    @Override
    public boolean update(String sessionId, String moduleSectionIdNullable, Date dateNullable, Integer weekNullable, Timestamp startTimeNullable, Timestamp endTimeNullable, String statusNullable) {
        Map<Field<?>, Object> changes = new HashMap<>();
        if (moduleSectionIdNullable != null) {
            changes.put(MODULE_SECTION_ID, moduleSectionIdNullable);
        }
        if (dateNullable != null) {
            changes.put(DATE, dateNullable);
        }
        if (weekNullable != null) {
            changes.put(WEEK, weekNullable);
        }
        if (startTimeNullable != null) {
            changes.put(START_TIME, startTimeNullable);
        }
        if (endTimeNullable != null) {
            changes.put(END_TIME, endTimeNullable);
        }
        if (statusNullable != null) {
            changes.put(STATUS, statusNullable);
        }
        if (changes.isEmpty()) {
            return false; // Nothing to update
        }
        return dsl.update(SESSIONS_TABLE)
            .set(changes)
            .where(SESSION_ID.eq(sessionId))
            .execute() > 0;
    }

    @Override
    public boolean deleteSessionById(String sessionId) {
        return dsl.deleteFrom(SESSIONS_TABLE)
            .where(SESSION_ID.eq(sessionId))
            .execute() > 0;
    }

    @Override
    public boolean deleteSessionsByModuleSectionId(String moduleSectionId) {
        return dsl.deleteFrom(SESSIONS_TABLE)
            .where(MODULE_SECTION_ID.eq(moduleSectionId))
            .execute() > 0;
    }
}
