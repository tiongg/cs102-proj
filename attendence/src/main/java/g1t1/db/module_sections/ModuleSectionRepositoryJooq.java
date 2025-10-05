package g1t1.db.module_sections;

import org.jooq.impl.DSL;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.Field;

import java.util.List;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class ModuleSectionRepositoryJooq implements ModuleSectionRepository {
    private final DSLContext dsl;

    private final Table<?> MODULE_SECTIONS_TABLE = DSL.table("module_sections");
    private final Field<String> MODULE_SECTION_ID = DSL.field("module_section_id", String.class);
    private final Field<String> MODULE_TITLE = DSL.field("module_title", String.class);
    private final Field<String> SECTION_NUMBER = DSL.field("section_number", String.class);
    private final Field<String> TERM = DSL.field("term", String.class);
    private final Field<Integer> DAY_OF_WEEK = DSL.field("day_of_week", Integer.class);
    private final Field<String> START_TIME = DSL.field("start_time", String.class);
    private final Field<String> END_TIME = DSL.field("end_time", String.class);
    private final Field<String> ROOM = DSL.field("room", String.class);
    private final Field<String> TEACHER_USER_ID = DSL.field("teacher_user_id", String.class);

    public ModuleSectionRepositoryJooq(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public String create(String moduleTitle, String sectionNumber, String term, int dayOfWeek, String startTime, String endTime, String room, String teacherUserId) {
        String uuid = UUID.randomUUID().toString();
        dsl.insertInto(MODULE_SECTIONS_TABLE)
            .set(MODULE_SECTION_ID, uuid)
            .set(MODULE_TITLE, moduleTitle)
            .set(SECTION_NUMBER, sectionNumber)
            .set(TERM, term)
            .set(DAY_OF_WEEK, dayOfWeek)
            .set(START_TIME, startTime)
            .set(END_TIME, endTime)
            .set(ROOM, room)
            .set(TEACHER_USER_ID, teacherUserId)
            .execute();
        return uuid;
    }

    @Override
    public List<ModuleSection> fetchModuleSectionsByModuleTitle(String moduleTitle) {
        return dsl.select(MODULE_SECTION_ID, MODULE_TITLE, SECTION_NUMBER, TERM, DAY_OF_WEEK, START_TIME, END_TIME, ROOM, TEACHER_USER_ID)
            .from(MODULE_SECTIONS_TABLE)
            .where(MODULE_TITLE.eq(moduleTitle))
            .fetch(record -> new ModuleSection(
                record.get(MODULE_SECTION_ID),
                record.get(MODULE_TITLE),
                record.get(SECTION_NUMBER),
                record.get(TERM),
                record.get(DAY_OF_WEEK),
                record.get(START_TIME),
                record.get(END_TIME),
                record.get(ROOM),
                record.get(TEACHER_USER_ID)
            ));
    }

    @Override
    public Optional<ModuleSection> fetchModuleSectionByModuleTitleAndSectionNumberAndTerm(String moduleTitle, String sectionNumber, String term) {
        return dsl.select(MODULE_SECTION_ID, MODULE_TITLE, SECTION_NUMBER, TERM, DAY_OF_WEEK, START_TIME, END_TIME, ROOM, TEACHER_USER_ID)
            .from(MODULE_SECTIONS_TABLE)
            .where(MODULE_TITLE.eq(moduleTitle))
            .and(SECTION_NUMBER.eq(sectionNumber))
            .and(TERM.eq(term))
            .fetchOptional(record -> new ModuleSection(
                record.get(MODULE_SECTION_ID),
                record.get(MODULE_TITLE),
                record.get(SECTION_NUMBER),
                record.get(TERM),
                record.get(DAY_OF_WEEK),
                record.get(START_TIME),
                record.get(END_TIME),
                record.get(ROOM),
                record.get(TEACHER_USER_ID)
            ));
    }

    @Override
    public List<ModuleSection> fetchModuleSectionsByTeacherUserId(String teacherUserId) {
        return dsl.select(MODULE_SECTION_ID, MODULE_TITLE, SECTION_NUMBER, TERM, DAY_OF_WEEK, START_TIME, END_TIME, ROOM, TEACHER_USER_ID)
            .from(MODULE_SECTIONS_TABLE)
            .where(TEACHER_USER_ID.eq(teacherUserId))
            .fetch(record -> new ModuleSection(
                record.get(MODULE_SECTION_ID),
                record.get(MODULE_TITLE),
                record.get(SECTION_NUMBER),
                record.get(TERM),
                record.get(DAY_OF_WEEK),
                record.get(START_TIME),
                record.get(END_TIME),
                record.get(ROOM),
                record.get(TEACHER_USER_ID)
            ));
    }

    @Override
    public boolean update(String moduleSectionId, String moduleTitleNullable, String sectionNumberNullable, String termNullable, String startTimeNullable, String endTimeNullable, String roomNullable, String teacherUserIdNullable) {
        var changes = new HashMap<Field<?>, Object>();
        if (moduleTitleNullable != null) changes.put(MODULE_TITLE, moduleTitleNullable);
        if (sectionNumberNullable != null) changes.put(SECTION_NUMBER, sectionNumberNullable);
        if (termNullable != null) changes.put(TERM, termNullable);
        if (startTimeNullable != null) changes.put(START_TIME, startTimeNullable);
        if (endTimeNullable != null) changes.put(END_TIME, endTimeNullable);
        if (roomNullable != null) changes.put(ROOM, roomNullable);
        if (teacherUserIdNullable != null) changes.put(TEACHER_USER_ID, teacherUserIdNullable);
        if (changes.isEmpty()) return false;

        return dsl.update(MODULE_SECTIONS_TABLE)
                .set(changes)
                .where(MODULE_SECTION_ID.eq(moduleSectionId))
                .execute() == 1;
    }

    @Override
    public boolean delete(String moduleSectionId) {
        return dsl.delete(MODULE_SECTIONS_TABLE)
            .where(MODULE_SECTION_ID.eq(moduleSectionId))
            .execute() == 1;
    }
}
