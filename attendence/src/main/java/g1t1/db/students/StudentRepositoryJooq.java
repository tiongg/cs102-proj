package g1t1.db.students;

import org.jooq.impl.DSL;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.Field;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StudentRepositoryJooq implements StudentRepository {
    private final DSLContext dsl;

    private final Table<?> STUDENTS_TABLE = DSL.table("students");
    private static final Field<String> STUDENT_ID = DSL.field("student_id", String.class);
    private static final Field<String> FULL_NAME = DSL.field("full_name", String.class);
    private static final Field<String> EMAIL = DSL.field("email", String.class);

    public StudentRepositoryJooq(DSLContext dsl) { this.dsl = dsl; }

    @Override
    public String create(String studentId, String fullName, String email) {
        dsl.insertInto(STUDENTS_TABLE)
            .set(STUDENT_ID, studentId)
            .set(FULL_NAME, fullName)
            .set(EMAIL, email)
            .execute();
        return studentId;
    }

    @Override
    public List<Student> fetchAllStudents() {
        return dsl.select(STUDENT_ID, FULL_NAME, EMAIL)
            .from(STUDENTS_TABLE)
            .fetch(record -> new Student(
                record.get(STUDENT_ID),
                record.get(FULL_NAME),
                record.get(EMAIL)
            ));
    }

    @Override 
    public Optional<Student> fetchStudentById(String studentId) {
        return dsl.select(STUDENT_ID, FULL_NAME, EMAIL)
            .from(STUDENTS_TABLE)
            .where(STUDENT_ID.eq(studentId))
            .fetchOptional(record -> new Student(
                record.get(STUDENT_ID),
                record.get(FULL_NAME),
                record.get(EMAIL)
        ));
    }

    @Override
    public Optional<Student> fetchStudentByEmail(String email) {
        return dsl.select(STUDENT_ID, FULL_NAME, EMAIL)
            .from(STUDENTS_TABLE)
            .where(EMAIL.eq(email))
            .fetchOptional(record -> new Student(
                record.get(STUDENT_ID),
                record.get(FULL_NAME),
                record.get(EMAIL)
        ));
    }

    @Override
    public boolean update(String studentId, String fullNameNullable, String emailNullable) {
        Map<Field<?>, Object> changes = new HashMap<>();
        if (fullNameNullable != null) changes.put(FULL_NAME, fullNameNullable);
        if (emailNullable != null) changes.put(EMAIL, emailNullable);
        if (changes.isEmpty()) return false;

        return dsl.update(STUDENTS_TABLE)
                .set(changes)
                .where(STUDENT_ID.eq(studentId))
                .execute() == 1;
    }

    @Override 
    public boolean delete(String studentId) {
        return dsl.delete(STUDENTS_TABLE)
            .where(STUDENT_ID.eq(studentId))
            .execute() == 1;
    }
}
