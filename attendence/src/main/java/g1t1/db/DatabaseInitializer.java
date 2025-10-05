package g1t1.db;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.sql.Connection;

public class DatabaseInitializer {
    private static final String URL = "jdbc:sqlite:database.db";

    public static void main(String[] args){
        DatabaseInitializer db = new DatabaseInitializer();
        db.init();
    }

    public void init() {
        try (Connection connection = connect(URL)) {
            DSLContext dsl = DSL.using(connection, SQLDialect.SQLITE);
        
            dsl.transaction(cfg -> {
                DSLContext context = DSL.using(cfg);

                createUsersTable(context);
                createStudentsTable(context);
                createUserFaceImagesTable(context);
                createStudentFaceImagesTable(context);
                createModuleSectionsTable(context);
                createEnrollmentsTable(context);
                createSessionsTable(context);
                createAttendancesTable(context);

                // optional: create indexes for optimizing search queries
            });        
        } catch (SQLException err) {
            // log error here
        } 
    }

    private Connection connect(String url) throws SQLException {
        /**
         * Obtain a JDBC connection from the given URL.
         *
         * Purpose
         * - Opens a database connection via DriverManager using the provided JDBC URL.
         *
         * Parameters
         * - url  JDBC connection string (e.g., "jdbc:sqlite:database.db").
         *
         * Returns
         * - A Connection when successful.
         * - null if the connection attempt fails.
         *
         * Behavior
         * - Obtains a JDBC connection via DriverManager.
         * - Throws SQLException if acquisition fails or returns null.
         * 
         */
        
        Connection connection = null;
        connection = DriverManager.getConnection(url);
        if (connection == null) {
            throw new SQLException("DriverManager returned null for URL: " + url);
        }
        return connection;
    }

    private void createUsersTable(DSLContext context) {
        /**
         * Create the `users` table if missing.
         *
         * Columns
         * - user_id        INT UNSIGNED, PK, not null. Surrogate key.
         * - full_name      VARCHAR(255), not null. Human-readable display name.
         * - email          VARCHAR(255), not null. Unique per user. Case-insensitive at application layer.
         * - password_hash  VARCHAR(255), not null. Stores a salted password hash.
         *
         * Constraints
         * - pk_users: primary key on user_id.
         * - unique_users_email: unique on email.
         *
         */

        context.createTableIfNotExists("users")
            .column("user_id", SQLDataType.INTEGERUNSIGNED.notNull())
            .column("full_name", SQLDataType.VARCHAR(255).notNull())
            .column("email", SQLDataType.VARCHAR(255).notNull())
            .column("password_hash", SQLDataType.VARCHAR(255).notNull())
            .constraints(
                DSL.constraint("pk_users").primaryKey("user_id"),
                DSL.constraint("unique_email").unique("email")
            )
            .execute();
    }

    private void createStudentsTable(DSLContext context) {
        /**
         * Create the `students` table if missing.
         *
         * Columns
         * - student_id     INT UNSIGNED, PK, not null. Matriculation number.
         * - full_name      VARCHAR(255), not null. Full name as per student ID card.
         * - email          VARCHAR(255), not null. Unique per user. Case-insensitive at application layer.
         *
         * Constraints
         * - pk_users: primary key on student_id.
         * - unique_users_email: unique on (email) to enforce a single user account per email.
         *
         */

        context.createTableIfNotExists("students")
            .column("student_id", SQLDataType.INTEGERUNSIGNED.notNull())
            .column("full_name", SQLDataType.VARCHAR(255).notNull())
            .column("email", SQLDataType.VARCHAR(255).notNull())
            .constraints(
                DSL.constraint("pk_students").primaryKey("student_id"),
                DSL.constraint("unique_email").unique("email")
            )
            .execute();
    }

    private void createUserFaceImagesTable(DSLContext context) {
        /**
         * Create the `user_face_images` table if missing.
         *
         * Columns
         * - face_image_id  INT UNSIGNED, PK, not null. Surrogate key for each image.
         * - user_id        INT UNSIGNED, not null. FK → users.user_id.
         * - face_image     BLOB, not null. Raw face image bytes.
         *
         * Constraints
         * - pk_user_face_images: primary key on face_image_id.
         * - fk_user_id: foreign key (user_id) → users(user_id) ON DELETE CASCADE.
         *
         * Notes
         * - Models a 1:N relationship from users to face images.
         * - Store multiple images per user by inserting multiple rows with the same user_id.
         * 
         */

        context.createTableIfNotExists("user_face_images")
        .column("face_image_id", SQLDataType.INTEGERUNSIGNED.notNull())
        .column("user_id", SQLDataType.INTEGERUNSIGNED.notNull())
        .column("face_image", SQLDataType.BLOB.notNull())
        .constraints(
            DSL.constraint("pk_user_face_images").primaryKey("face_image_id"),
            DSL.constraint("fk_user_id")
                .foreignKey("user_id")
                .references("users", "user_id")
                .onDeleteCascade()
        )
        .execute();
    }
    
    private void createStudentFaceImagesTable(DSLContext context) {
        /**
         * Create the `student_face_images` table if missing.
         *
         * Columns
         * - face_image_id  INT UNSIGNED, PK, not null. Surrogate key for each image.
         * - student_id     INT UNSIGNED, not null. FK → students.student_id.
         * - face_image     BLOB, not null. Raw face image bytes.
         *
         * Constraints
         * - pk_student_face_images: primary key on face_image_id.
         * - fk_student_id: foreign key (student_id) → students(student_id) ON DELETE CASCADE.
         *
         * Notes
         * - Models a 1:N relationship from students to face images.
         * - Store multiple images per student by inserting multiple rows with the same student_id.
         * 
         */

        context.createTableIfNotExists("student_face_images")
            .column("face_image_id", SQLDataType.INTEGERUNSIGNED.notNull())
            .column("student_id", SQLDataType.INTEGERUNSIGNED.notNull())
            .column("face_image", SQLDataType.BLOB.notNull())
            .constraints(
                DSL.constraint("pk_student_face_images").primaryKey("face_image_id"),
                DSL.constraint("fk_student_id")
                    .foreignKey("student_id")
                    .references("students", "student_id")
                    .onDeleteCascade()
            )
            .execute();
    }

    private void createModuleSectionsTable(DSLContext context) {
        /**
         * Create the `module_sections` table if missing.
         *
         * Columns
         * - module_section_id  INT UNSIGNED, PK, not null. Surrogate key.
         * - module_title       VARCHAR, not null. Title of the module (e.g, "CS102 Programming Fundamentals II").
         * - section_number     VARCHAR(3), not null. Short code per module (e.g., "G1").
         * - term               VARCHAR, not null. Academic term label (e.g., "AY24/25 Term 1").
         * - day_of_week        TINYINT UNSIGNED, not null. 1 = Mon … 7 = Sun.
         * - start_time         VARCHAR(8), not null. Time string "HH:MM AM/PM".
         * - end_time           VARCHAR(8), not null. Time string "HH:MM AM/PM".
         * - room               VARCHAR, not null. Location identifier.
         * - teacher_user_id    INT UNSIGNED, not null. FK → users.user_id.
         *
         * Constraints
         * - pk_module_sections: primary key on module_section_id.
         * - unique_module_title_section_number_term: unique on (module_title, section_number, term) to prevent duplicate module sections during the same term.
         * - fk_teacher_user_id on (teacher_user_id) → users(user_id) ON DELETE RESTRICT.
         * 
         * Notes
         * - ON DELETE RESTRICT prevents deleting a user if any module_sections reference them.
         *
         */

        context.createTableIfNotExists("module_sections")
            .column("module_section_id", SQLDataType.INTEGERUNSIGNED.notNull())
            .column("module_title", SQLDataType.VARCHAR.notNull())
            .column("section_number", SQLDataType.VARCHAR(3).notNull())
            .column("term", SQLDataType.VARCHAR.notNull())
            .column("day_of_week", SQLDataType.TINYINTUNSIGNED.notNull())  
            .column("start_time", SQLDataType.VARCHAR(8).notNull()) 
            .column("end_time", SQLDataType.VARCHAR(8).notNull())   
            .column("room", SQLDataType.VARCHAR.notNull())
            .column("teacher_user_id", SQLDataType.INTEGERUNSIGNED.notNull())
            .constraints(
                DSL.constraint("pk_module_sections").primaryKey("module_section_id"),
                DSL.constraint("unique_module_title_section_number_term")
                    .unique("module_title", "section_number", "term"),
                DSL.constraint("fk_teacher_user_id")
                    .foreignKey("teacher_user_id").references("users", "user_id")
                    .onDeleteRestrict()
               )
            .execute();
    }

    private void createEnrollmentsTable(DSLContext context) {
        /**
         * Create the `enrollments` table if missing.
         *
         * Columns
         * - enrollment_id      INT UNSIGNED, PK, not null. Surrogate key.
         * - module_section_id  INT UNSIGNED, not null. FK to module_sections.module_section_id.
         * - student_id         INT UNSIGNED, not null. FK to student.student_id.
         *
         * Constraints
         * - pk_enrollment: primary key on enrollment_id.
         * - unique_section_id_student_id: unique on (module_section_id, student_id) to prevent duplicate enrollments.
         * - fk_enrollment_module_section_id: foreign key (module_section_id) → module_sections(module_section_id) ON DELETE CASCADE.
         * - fk_enrollment_student_id: foreign key (student_id) → students(student_id) ON DELETE CASCADE.
         *
         * Notes
         * - Composite uniqueness enforces one active seat per student per section.
         * - Cascade deletes remove enrollments when a module section or user is deleted.
         * 
         */

        context.createTableIfNotExists("enrollments")
               .column("enrollment_id", SQLDataType.INTEGERUNSIGNED.notNull())
               .column("module_section_id", SQLDataType.INTEGERUNSIGNED.notNull())
               .column("student_id", SQLDataType.INTEGERUNSIGNED.notNull())
               .constraints(
                   DSL.constraint("pk_enrollments").primaryKey("enrollment_id"),
                   DSL.constraint("unique_module_section_id_student_id").unique("module_section_id", "student_id"),
                   DSL.constraint("fk_enrollment_section_id")
                       .foreignKey("module_section_id")
                       .references("module_sections", "module_section_id")
                       .onDeleteCascade(),
                   DSL.constraint("fk_enrollment_student_id")
                       .foreignKey("student_id")
                       .references("students", "student_id")
                       .onDeleteCascade()
               )
               .execute();
    }

    private void createSessionsTable(DSLContext context) {
        /**
         * Create the `sessions` table if missing.
         *
         * Columns
         * - session_id         INT UNSIGNED, PK, auto-increment, not null. Surrogate key.
         * - module_section_id  INT UNSIGNED, not null. FK → module_sections.module_section_id.
         * - date               DATE, not null. Calendar date of the meeting.
         * - week               TINYINT UNSIGNED, not null. 1–13 inclusive.
         * - start_time         TIMESTAMP, not null. Start timestamp.
         * - end_time           TIMESTAMP, not null. End timestamp.
         * - status             VARCHAR(16), not null. {"ongoing","completed"}.
         * - created_at         TIMESTAMP, not null. Default CURRENT_TIMESTAMP.
         *
         * Constraints
         * - pk_sessions: primary key on session_id.
         * - fk_sessions_module_section_id: foreign key (module_section_id) → module_sections(module_section_id) ON DELETE CASCADE.
         * - unique_module_section_id_date_week: unique on (module_section_id, date, week) to prevent duplicate sessions.
         * - ck_sessions_status: check status ∈ {"ongoing","completed"}.
         * 
         */

        context.createTableIfNotExists("sessions")
            .column("session_id", SQLDataType.INTEGERUNSIGNED.identity(true).notNull())
            .column("module_section_id", SQLDataType.INTEGERUNSIGNED.notNull())
            .column("date", SQLDataType.DATE.notNull())
            .column("week", SQLDataType.TINYINTUNSIGNED.notNull())
            .column("start_time", SQLDataType.TIMESTAMP.notNull())
            .column("end_time", SQLDataType.TIMESTAMP.notNull())
            .column("status", SQLDataType.VARCHAR(16).notNull())
            .column("created_at", SQLDataType.TIMESTAMP.defaultValue(DSL.currentTimestamp()).notNull())
            .constraints(
                DSL.constraint("pk_sessions").primaryKey("session_id"),
                DSL.constraint("fk_sessions_module_section_id")
                    .foreignKey("module_section_id").references("module_sections", "module_section_id")
                    .onDeleteCascade(),
                DSL.constraint("unique_module_section_id_date_week").unique("module_section_id", "date", "week"),
                DSL.constraint("ck_sessions_status")
                    .check(DSL.field("status", String.class).in("ongoing", "completed"))
            )
            .execute();
    }

    private void createAttendancesTable(DSLContext context) {
        /**
         * Create the `attendance` table if missing.
         *
         * Columns
         * - session_id          INT UNSIGNED, not null. FK → sessions.session_id.
         * - enrollment_id       INT UNSIGNED, not null. FK → enrollments.enrollment_id.
         * - status              VARCHAR(16), not null. {"present","late","absent","excused"}.
         * - recorded_timestamp  TIMESTAMP, not null. Default CURRENT_TIMESTAMP.
         *
         * Constraints
         * - pk_attendance: primary key on (session_id, enrollment_id).
         * - fk_attendance_session_id: foreign key (session_id) → sessions(session_id) ON DELETE CASCADE.
         * - fk_attendance_enrollment_id: foreign key (enrollment_id) → enrollments(enrollment_id) ON DELETE CASCADE.
         * - ck_attendance_status: check status ∈ {"present","late","absent","excused"}.
         * 
         */

        context.createTableIfNotExists("attendance")
        .column("session_id", SQLDataType.INTEGERUNSIGNED.notNull())
        .column("enrollment_id", SQLDataType.INTEGERUNSIGNED.notNull())
        .column("status", SQLDataType.VARCHAR(16).notNull())
        .column("recorded_timestamp", SQLDataType.TIMESTAMP.defaultValue(DSL.currentTimestamp()).notNull())
        .constraints(
            DSL.constraint("pk_attendance").primaryKey("session_id", "enrollment_id"),
            DSL.constraint("fk_attendance_session_id")
                .foreignKey("session_id").references("sessions", "session_id")
                .onDeleteCascade(),
            DSL.constraint("fk_attendance_enrollment_id")
                .foreignKey("enrollment_id").references("enrollments", "enrollment_id")
                .onDeleteCascade(),
            DSL.constraint("ck_attendance_status")
                .check(DSL.field("status", String.class).in("present", "late", "absent", "excused"))
        )
        .execute();
    }
}
