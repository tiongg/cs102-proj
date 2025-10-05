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
        try {
            Connection connection = connect(URL);
            DSLContext dsl = DSL.using(connection, SQLDialect.SQLITE);
        
            dsl.transaction(cfg -> {
                DSLContext context = DSL.using(cfg);

                createUsersTable(context);
                createModulesTable(context);
                createModuleSectionsTable(context);
                createEnrollmentsTable(context);
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
         * - Catches SQLException, logs the message to stderr, and returns null.
         * - Declares SQLException in the signature, but does not rethrow it.
         *
         * Caller Responsibilities
         * - Check the returned value for null before use.
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
         * - password_hash  VARCHAR(255), not null. Stores a salted password hash 
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
                DSL.constraint("unique_users_email").unique("email")
            )
            .execute();
    }

    private void createModulesTable(DSLContext context) {
        /**
         * Create the `modules` table if missing.
         *
         * Columns
         * - module_id   INT UNSIGNED, not null. Surrogate key.
         * - title       VARCHAR(255), not null. Human-readable module title.
         *
         * Constraints
         * - pk_modules: primary key on module_id.
         * - unique_module_title: unique on title.
         * 
         */

        context.createTableIfNotExists("modules")
            .column("module_id", SQLDataType.INTEGERUNSIGNED.notNull())
            .column("title", SQLDataType.VARCHAR(255).notNull())
            .constraints(
                DSL.constraint("pk_modules").primaryKey("module_id"),
                DSL.constraint("unique_module_title").unique("title")
            )
            .execute();
    }

    private void createModuleSectionsTable(DSLContext context) {
        /**
         * Create the `module_sections` table if missing.
         *
         * Columns
         * - section_id      INT UNSIGNED, PK, not null. Surrogate key.
         * - module_id       INT UNSIGNED, not null. FK to modules.module_id.
         * - term            VARCHAR, not null. Academic term label (e.g., "AY24/25 Term 1").
         * - section_number  VARCHAR(3), not null. Short code per module (e.g., "G1").
         * - day_of_week     TINYINT UNSIGNED, not null. 1=Mon … 7=Sun.
         * - start_time      VARCHAR, not null. Time string "HH:MM" 24h.
         * - end_time        VARCHAR, not null. Time string "HH:MM" 24h.
         * - room            VARCHAR, not null. Location identifier.
         *
         * Constraints
         * - pk_section: primary key on section_id.
         * - unique_module_id_term_section_number: unique on (module_id, term, section_number) to prevent duplicate module sections per term.
         * - fk_module_section_module_id: foreign key (module_id) → modules(module_id) ON DELETE CASCADE.
         *
         */

        context.createTableIfNotExists("module_sections")
            .column("section_id", SQLDataType.INTEGERUNSIGNED.notNull())
            .column("module_id", SQLDataType.INTEGERUNSIGNED.notNull())
            .column("term", SQLDataType.VARCHAR.notNull())
            .column("section_number", SQLDataType.VARCHAR(3).notNull())
            .column("day_of_week", SQLDataType.TINYINTUNSIGNED.notNull())  // 1 - 7
            .column("start_time", SQLDataType.VARCHAR.notNull())           // 'HH:MM'
            .column("end_time", SQLDataType.VARCHAR.notNull())             // 'HH:MM'
            .column("room", SQLDataType.VARCHAR.notNull())
            .constraints(
                DSL.constraint("pk_section").primaryKey("section_id"),
                DSL.constraint("unique_module_id_term_section_number")
                    .unique("module_id", "term", "section_number"),
                DSL.constraint("fk_module_section_module_id")
                    .foreignKey("module_id")
                    .references("modules", "module_id")
                    .onDeleteCascade()
               )
               .execute();

    }
    private void createEnrollmentsTable(DSLContext context) {
        /**
         * Create the `enrollments` table if missing.
         *
         * Columns
         * - enrollment_id  INT UNSIGNED, PK, not null. Surrogate key.
         * - section_id     INT UNSIGNED, not null. FK to module_sections.section_id.
         * - student_id     INT UNSIGNED, not null. FK to users.user_id.
         *
         * Constraints
         * - pk_enrollment: primary key on enrollment_id.
         * - unique_section_id_student_id: unique on (section_id, student_id) to prevent duplicate enrollments.
         * - fk_enrollment_section_id: foreign key (section_id) → module_sections(section_id) ON DELETE CASCADE.
         * - fk_enrollment_student_id: foreign key (student_id) → users(user_id) ON DELETE CASCADE.
         *
         * Notes
         * - Composite uniqueness enforces one active seat per student per section.
         * - Cascade deletes remove enrollments when a section or user is deleted.
         * 
         */

        context.createTableIfNotExists("enrollments")
               .column("enrollment_id", SQLDataType.INTEGERUNSIGNED.notNull())
               .column("section_id", SQLDataType.INTEGERUNSIGNED.notNull())
               .column("student_id", SQLDataType.INTEGERUNSIGNED.notNull())
               .constraints(
                   DSL.constraint("pk_enrollment").primaryKey("enrollment_id"),
                   DSL.constraint("unique_section_id_student_id").unique("section_id", "student_id"),
                   DSL.constraint("fk_enrollment_section_id")
                       .foreignKey("section_id")
                       .references("module_sections", "section_id")
                       .onDeleteCascade(),
                   DSL.constraint("fk_enrollment_student_id")
                       .foreignKey("student_id")
                       .references("users", "user_id")
                       .onDeleteCascade()
               )
               .execute();
    }

    private void createAttendancesTable(DSLContext context) {
        /**
         * Create the `attendance` table if missing.
         *
         * Columns
         * - enrollment_id       INT UNSIGNED, not null. FK to enrollments.enrollment_id.
         * - week                TINYINT UNSIGNED, not null. 1–13 inclusive.
         * - date                DATE, not null. Class session calendar date.
         * - status              VARCHAR, not null. One of: "present" | "late" | "absent" | "excused".
         * - recorded_timestamp  TIMESTAMP, not null. Server-side record time.
         *
         * Constraints
         * - pk_attendance: composite primary key on (enrollment_id, week, date).
         * - fk_attendance_enrollment_id: foreign key (enrollment_id) → enrollments(enrollment_id) ON DELETE CASCADE.
         * - ck_attendance_status: check constraint enforcing status ∈ {present, late, absent, excused}.
         * 
         * Notes
         * - Composite PK enforces a single record per enrollment-week-date.
         * 
         */

        context.createTableIfNotExists("attendance")
               .column("enrollment_id", SQLDataType.INTEGERUNSIGNED.notNull())
               .column("week", SQLDataType.TINYINTUNSIGNED.notNull())  // 1 - 13
               .column("date", SQLDataType.DATE.notNull())
               .column("status", SQLDataType.VARCHAR.notNull())      // present|late|absent|excused
               .column("recorded_timestamp", SQLDataType.TIMESTAMP.notNull())
               .constraints(
                   DSL.constraint("pk_attendance").primaryKey("enrollment_id", "week", "date"),
                   DSL.constraint("fk_attendance_enrollment_id")
                       .foreignKey("enrollment_id")
                       .references("enrollments", "enrollment_id")
                       .onDeleteCascade(),
                    DSL.constraint("ck_attendance_status")
                        .check(DSL.field("status", String.class).in("present", "late", "absent", "excused"))
               )
               .execute();
    }

}
