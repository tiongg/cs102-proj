package g1t1.db.enrollments;

import org.jooq.impl.DSL;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.Field;

import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

public class EnrollmentRepositoryJooq implements EnrollmentRepository {
    private final DSLContext dsl;

    private final Table<?> ENROLLMENTS_TABLE = DSL.table("enrollments");
    private final Field<String> ENROLLMENT_ID = DSL.field("enrollment_id", String.class);
    private final Field<String> MODULE_SECTION_ID = DSL.field("module_section_id", String.class);
    private final Field<String> STUDENT_ID = DSL.field("student_id", String.class);

    public EnrollmentRepositoryJooq(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public String create(String moduleSectionId, String studentId) {
        String uuid = UUID.randomUUID().toString();
        dsl.insertInto(ENROLLMENTS_TABLE)
            .set(ENROLLMENT_ID, uuid)
            .set(MODULE_SECTION_ID, moduleSectionId)
            .set(STUDENT_ID, studentId)
            .execute();
        return uuid;
    }

    @Override
    public Optional<Enrollment> fetchEnrollmentById(String enrollmentId) {
        return dsl.select(ENROLLMENT_ID, MODULE_SECTION_ID, STUDENT_ID)
            .from(ENROLLMENTS_TABLE)
            .where(ENROLLMENT_ID.eq(enrollmentId))
            .fetchOptional(record -> new Enrollment(
                record.get(ENROLLMENT_ID),
                record.get(MODULE_SECTION_ID),
                record.get(STUDENT_ID)
            ));
    }

    @Override
    public List<Enrollment> fetchEnrollmentsByModuleSectionId(String moduleSectionId) {
        return dsl.select(ENROLLMENT_ID, MODULE_SECTION_ID, STUDENT_ID)
            .from(ENROLLMENTS_TABLE)
            .where(MODULE_SECTION_ID.eq(moduleSectionId))
            .fetch(record -> new Enrollment(
                record.get(ENROLLMENT_ID),
                record.get(MODULE_SECTION_ID),
                record.get(STUDENT_ID)
            ));
    }

    @Override
    public List<Enrollment> fetchEnrollmentsByStudentId(String studentId) {
        return dsl.select(ENROLLMENT_ID, MODULE_SECTION_ID, STUDENT_ID)
            .from(ENROLLMENTS_TABLE)
            .where(STUDENT_ID.eq(studentId))
            .fetch(record -> new Enrollment(
                record.get(ENROLLMENT_ID),
                record.get(MODULE_SECTION_ID),
                record.get(STUDENT_ID)
            ));
    }

    @Override
    public boolean update(String enrollmentId, String moduleSectionIdNullable, String studentIdNullable) {
        Map<Field<?>, Object> changes = new HashMap<>();
        if (moduleSectionIdNullable != null) {
            changes.put(MODULE_SECTION_ID, moduleSectionIdNullable);
        }
        if (studentIdNullable != null) {
            changes.put(STUDENT_ID, studentIdNullable);
        }
        if (changes.isEmpty()) {
            return false;
        }
        return dsl.update(ENROLLMENTS_TABLE)
            .set(changes)
            .where(ENROLLMENT_ID.eq(enrollmentId))
            .execute() > 0;
    }

    @Override
    public boolean deleteById(String enrollmentId) {
        return dsl.deleteFrom(ENROLLMENTS_TABLE)
            .where(ENROLLMENT_ID.eq(enrollmentId))
            .execute() > 0;
    }

    @Override
    public boolean deleteByModuleSectionId(String moduleSectionId) {
        return dsl.deleteFrom(ENROLLMENTS_TABLE)
            .where(MODULE_SECTION_ID.eq(moduleSectionId))
            .execute() > 0;
    }

    @Override
    public boolean deleteByStudentId(String studentId) {
        return dsl.deleteFrom(ENROLLMENTS_TABLE)
            .where(STUDENT_ID.eq(studentId))
            .execute() > 0;
    }
}
