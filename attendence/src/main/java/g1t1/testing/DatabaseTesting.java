package g1t1.testing;

import g1t1.db.DatabaseInitializer;
import g1t1.db.attendance.Attendance;
import g1t1.db.attendance.AttendanceRepository;
import g1t1.db.attendance.AttendanceRepositoryJooq;
import g1t1.db.enrollments.Enrollment;
import g1t1.db.enrollments.EnrollmentRepository;
import g1t1.db.enrollments.EnrollmentRepositoryJooq;
import g1t1.db.module_sections.ModuleSection;
import g1t1.db.module_sections.ModuleSectionRepository;
import g1t1.db.module_sections.ModuleSectionRepositoryJooq;
import g1t1.db.sessions.Session;
import g1t1.db.sessions.SessionRepository;
import g1t1.db.sessions.SessionRepositoryJooq;
import g1t1.db.students.Student;
import g1t1.db.students.StudentRepository;
import g1t1.db.students.StudentRepositoryJooq;
import g1t1.db.users.User;
import g1t1.db.users.UserRepository;
import g1t1.db.users.UserRepositoryJooq;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DatabaseTesting {
    public static void main(String[] args) {
        DatabaseInitializer db = new DatabaseInitializer();
        db.init();

        try (Connection connection = db.connect()) {
            DSLContext dsl = DSL.using(connection, SQLDialect.SQLITE);

            UserRepository userRepo = new UserRepositoryJooq(dsl);
            StudentRepository studentRepo = new StudentRepositoryJooq(dsl);
            ModuleSectionRepository moduleSectionRepo = new ModuleSectionRepositoryJooq(dsl);
            SessionRepository sessionRepo = new SessionRepositoryJooq(dsl);
            EnrollmentRepository enrollmentRepo = new EnrollmentRepositoryJooq(dsl);
            AttendanceRepository attendanceRepo = new AttendanceRepositoryJooq(dsl);

            List<String> userIds = testUserRepo(userRepo);
            List<String> studentIds = testStudentRepo(studentRepo);
            List<String> moduleSectionIds = testModuleSectionRepo(moduleSectionRepo, userIds);
            List<String> enrollmentIds = testEnrollmentRepo(enrollmentRepo, studentIds, moduleSectionIds);
            List<String> sessionIds = testSessionRepo(sessionRepo, moduleSectionIds, userIds);
            testAttendanceRepo(attendanceRepo, sessionIds, enrollmentIds);

            clearAllTables(dsl);

        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
        } catch (DataAccessException e) {
            System.out.println("Error during database operation: " + e.getMessage());
        }
    }


    private static List<String> testUserRepo(UserRepository userRepo) {
        // Create users
        String idA = userRepo.create(UUID.randomUUID().toString(), "Harry Ng", "harryngkokjing@gmail.com", "pA");
        String idB = userRepo.create(UUID.randomUUID().toString(), "Jared Chan", "jaredchan@gmail.com", "pB");
        String idC = userRepo.create(UUID.randomUUID().toString(), "Tiong Guan", "tiongguan@gmail.com", "pC");
        String idD = userRepo.create(UUID.randomUUID().toString(), "Flash Teng", "flashteng@gmail.com", "pD");
        System.out.println("Inserted IDs: " + List.of(idA, idB, idC, idD));

        // Read all users
        List<User> all = userRepo.fetchAllUsers();
        System.out.println("All users count = " + all.size());
        dumpUsers("All users", all);

        // Fetch user by id/email
        System.out.println("Fetch by id(user B): " + userRepo.fetchUserById(idB));
        System.out.println("Fetch by email(user C): " + userRepo.fetchUserByEmail("tiongguan@gmail.com"));

        // Update name only
        boolean updD = userRepo.update(idD, "Phimosis King Flash", null);
        System.out.println("Update user C result = " + updD + " -> " + userRepo.fetchUserById(idD));

        // Update name+email 
        boolean updA = userRepo.update(idA, "Kok Jing", "kokjing@gmail.com");
        System.out.println("Update user A result = " + updA + " -> " + userRepo.fetchUserById(idA));

        // Update password
        boolean pwB = userRepo.updatePassword(idB, "pB2");
        System.out.println("Update user B password = " + pwB);

        // Delete one
        boolean delC = userRepo.delete(idC);
        System.out.println("Delete user C = " + delC + ", fetch C -> " + userRepo.fetchUserById(idC));

        // Final state
        List<User> remaining = userRepo.fetchAllUsers();
        dumpUsers("Remaining users", remaining);

        return List.of(idA, idB, idD);
    }

    /**
     * Utility for displaying user records in console
     */
    private static void dumpUsers(String title, List<User> users) {
        System.out.println("=== " + title + " ===");
        for (User u : users) {
            System.out.println(u.userId() + " | " + u.fullName() + " | " + u.email() + " | " + u.passwordHash());
        }
    }

    private static List<String> testStudentRepo(StudentRepository studentRepo) {
        // Create students
        String idA = studentRepo.create("01468214", "Mokkie", "mok@smu.edu.sg");
        String idB = studentRepo.create("01468213", "Guo Lai", "guolai@smu.edu.sg");
        String idC = studentRepo.create("01468212", "Jason Chan", "jasonchan@smu.edu.sg");
        String idD = studentRepo.create("01468211", "Guomin", "guomin@smu.edu.sg");
        System.out.println("Inserted Student IDs: " + List.of(idA, idB, idC, idD));

        // Read all students
        List<Student> all = studentRepo.fetchAllStudents();
        System.out.println("All students count = " + all.size());
        dumpStudents("All students", all);

        // Fetch student by id/email
        System.out.println("Fetch by id(student C): " + studentRepo.fetchStudentById(idC));
        System.out.println("Fetch by email(student D): " + studentRepo.fetchStudentByEmail("guomin@smu.edu.sg"));

        // Update name only
        boolean updA = studentRepo.update(idA, "Mok Heng Ngee", null);
        System.out.println("Update student A result = " + updA + " -> " + studentRepo.fetchStudentById(idA));

        // Update name + email
        boolean updB = studentRepo.update(idB, "Zhang Zhiyuan", "zhangzhiyuan@gmail.com");
        System.out.println("Update student B result = " + updB + " -> " + studentRepo.fetchStudentById(idB));

        // Delete one
        boolean delD = studentRepo.delete(idD);
        System.out.println("Delete student D = " + delD + ", fetch D -> " + studentRepo.fetchStudentById(idD));

        // Final state
        List<Student> remaining = studentRepo.fetchAllStudents();
        dumpStudents("Remaining students", remaining);

        return List.of(idA, idB, idC);
    }

    /**
     * Utility for displaying student records in console
     */
    private static void dumpStudents(String title, List<Student> students) {
        System.out.println("=== " + title + " ===");
        for (Student s : students) {
            System.out.println(s.studentId() + " | " + s.fullName() + " | " + s.email());
        }
    }

    private static List<String> testModuleSectionRepo(ModuleSectionRepository moduleSectionRepo, List<String> teacherUserIds) {
        // Create module sections
        String idA = moduleSectionRepo.create("CS102 Programming Fundamentals II", "G1", "AY2025/2026 Term 1", 3, "08:15 AM", "11:30 AM", "SCIS1 SR 2-4", teacherUserIds.get(0));
        String idB = moduleSectionRepo.create("CS102 Programming Fundamentals II", "G2", "AY2025/2026 Term 1", 3, "03:30 PM", "06:45 PM", "SCIS1 SR 2-4", teacherUserIds.get(0));
        String idC = moduleSectionRepo.create("IS216 Web Application Development II", "G5", "AY2025/2026 Term 1", 4, "08:15 PM", "11:30 AM", "SCIS1 SR 2-3", teacherUserIds.get(0));
        String idD = moduleSectionRepo.create("IS115 Algorithms & Programming", "G13", "AY2025/2026 Term 1", 2, "12:00 PM", "3:15 PM", "SCIS1 SR 3-1", teacherUserIds.get(1));
        String idE = moduleSectionRepo.create("SE301 Operating Systems & Networks", "G7", "AY2024/2025 Term 1", 5, "03:30 PM", "06:45 PM", "SCIS2/SOE SR B1-1 ", teacherUserIds.get(2));
        System.out.println("Inserted Module Section IDs: " + List.of(idA, idB, idC, idD, idE));

        // Read all module sections
        List<ModuleSection> cs102ModuleSections = moduleSectionRepo.fetchModuleSectionsByModuleTitle("CS102 Programming Fundamentals II");
        System.out.println("All CS102 module sections count = " + cs102ModuleSections.size());
        dumpModuleSections("All CS102 module sections", cs102ModuleSections);

        // Fetch module section by module title + section number + term
        System.out.println("Fetch by module title + section number + term (SE301 G7 AY2024/2025 Term 1): " + moduleSectionRepo.fetchModuleSectionByModuleTitleAndSectionNumberAndTerm("SE301 Operating Systems & Networks", "G7", "AY2024/2025 Term 1"));

        // Fetch module sections by teacher user id
        List<ModuleSection> sectionsByKokJing = moduleSectionRepo.fetchModuleSectionsByTeacherUserId(teacherUserIds.get(0));
        System.out.println("Fetch by teacher user id (Kok Jing) count = " + sectionsByKokJing.size());
        dumpModuleSections("Module sections taught by Kok Jing", sectionsByKokJing);

        // Update room only
        boolean updA = moduleSectionRepo.update(idA, null, null, null, null, null, "SCIS1 SR 2-5", null);
        System.out.println("Update module section A result = " + updA + " -> " + moduleSectionRepo.fetchModuleSectionByModuleTitleAndSectionNumberAndTerm("CS102 Programming Fundamentals II", "G1", "AY2025/2026 Term 1"));

        // Update multiple fields
        boolean updB = moduleSectionRepo.update(idB, null, null, null, "7:00PM", "10:15 PM", "SCIS1 SR 2-5", teacherUserIds.get(1));
        System.out.println("Update module section B result = " + updB + " -> " + moduleSectionRepo.fetchModuleSectionByModuleTitleAndSectionNumberAndTerm("CS102 Programming Fundamentals II", "G2", "AY2025/2026 Term 1"));

        // Delete one
        boolean delE = moduleSectionRepo.delete(idE);
        System.out.println("Delete module section E = " + delE + ", fetch E -> " + moduleSectionRepo.fetchModuleSectionByModuleTitleAndSectionNumberAndTerm("SE301 Operating Systems & Networks", "G7", "AY2024/2025 Term 1"));

        // Final state
        List<ModuleSection> remaining = moduleSectionRepo.fetchModuleSectionsByModuleTitle("CS102 Programming Fundamentals II");
        dumpModuleSections("Remaining CS102 module sections", remaining);

        return List.of(idA, idB, idC, idD);
    }

    private static void dumpModuleSections(String title, List<ModuleSection> sections) {
        System.out.println("=== " + title + " ===");
        for (ModuleSection ms : sections) {
            System.out.println(ms.moduleSectionId() + " | " + ms.moduleTitle() + " | " + ms.sectionNumber() + " | " + ms.dayOfWeek() + " | " + ms.startTime() + " - " + ms.endTime() + " | " + ms.room() + " | " + ms.teacherUserId());
        }
    }

    private static List<String> testEnrollmentRepo(EnrollmentRepository enrollmentRepo, List<String> studentIds, List<String> moduleSectionIds) {
        // Create enrollments
        String idA = enrollmentRepo.create(moduleSectionIds.get(0), studentIds.get(0));
        String idB = enrollmentRepo.create(moduleSectionIds.get(0), studentIds.get(1));

        // Fetch by id
        System.out.println("Fetch by id(enrollment A): " + enrollmentRepo.fetchEnrollmentById(idA));

        // Fetch by module section id
        List<Enrollment> enrollmentsByModuleSection = enrollmentRepo.fetchEnrollmentsByModuleSectionId(moduleSectionIds.get(0));
        System.out.println("Fetch by module section id count = " + enrollmentsByModuleSection.size());
        dumpEnrollments("Enrollments for module section " + moduleSectionIds.get(0), enrollmentsByModuleSection);

        // Fetch by student id
        List<Enrollment> enrollmentsByStudent = enrollmentRepo.fetchEnrollmentsByStudentId(studentIds.get(0));
        System.out.println("Fetch by student id count = " + enrollmentsByStudent.size());
        dumpEnrollments("Enrollments for student " + studentIds.get(0), enrollmentsByStudent);

        // Delete by Student Id
        boolean delByStudent = enrollmentRepo.deleteByStudentId(studentIds.get(1));
        System.out.println("Delete enrollment by student id = " + delByStudent + ", fetch B -> " + enrollmentRepo.fetchEnrollmentById(idB));

        return List.of(idA);
    }

    private static void dumpEnrollments(String title, List<Enrollment> enrollments) {
        System.out.println("=== " + title + " ===");
        for (Enrollment e : enrollments) {
            System.out.println(e.enrollmentId() + " | " + e.studentId() + " | " + e.moduleSectionId());
        }
    }

    private static List<String> testSessionRepo(SessionRepository sessionRepo, List<String> moduleSectionIds, List<String> teacherUserIds) {
        // Create sessions
        String idA = sessionRepo.create(moduleSectionIds.get(0), Date.valueOf("2025-08-20"), (short) 1, Timestamp.valueOf("2025-08-20 08:15:00"), null, "ongoing");
        String idB = sessionRepo.create(moduleSectionIds.get(0), Date.valueOf("2025-08-27"), (short) 2, Timestamp.valueOf("2025-08-27 08:15:00"), null, "ongoing");
        System.out.println("Inserted Session IDs: " + List.of(idA, idB));

        // Fetch session by id 
        System.out.println("Fetch by id(session A): " + sessionRepo.fetchSessionById(idA));

        // Fetch sessions by module section id
        List<Session> sessionsByModuleSection = sessionRepo.fetchSessionsByModuleSectionId(moduleSectionIds.get(0));
        System.out.println("Fetch by module section id count = " + sessionsByModuleSection.size());
        dumpSessions("Sessions for module section " + moduleSectionIds.get(0), sessionsByModuleSection);

        // Fetch sessions by teacher user id + week
        List<Session> sessionsByTeacherAndWeek = sessionRepo.fetchSessionsByTeacherUserIdAndWeek(teacherUserIds.get(0), (short) 1);
        System.out.println("Fetch by teacher user id + week count = " + sessionsByTeacherAndWeek.size());
        dumpSessions("Sessions for teacher " + teacherUserIds.get(0) + " in week 1", sessionsByTeacherAndWeek);

        // Update end time + status
        boolean updA = sessionRepo.update(idA, null, null, null, null, Timestamp.valueOf("2025-08-20 11:30:00"), "completed");
        System.out.println("Update session A result = " + updA + " -> " + sessionRepo.fetchSessionById(idA));

        // Delete one
        boolean delB = sessionRepo.deleteSessionById(idB);
        System.out.println("Delete session B = " + delB + ", fetch B -> " + sessionRepo.fetchSessionById(idB));

        return List.of(idA);
    }

    private static void dumpSessions(String title, List<Session> sessions) {
        System.out.println("=== " + title + " ===");
        for (Session s : sessions) {
            System.out.println(s.sessionId() + " | " + s.moduleSectionId() + " | " + s.date() + " | Week " + s.week() + " | " + s.startTime() + " - " + s.endTime() + " | " + s.status() + " | Created at: " + s.createdAt());
        }
    }

    private static void testAttendanceRepo(AttendanceRepository attendanceRepo, List<String> sessionIds, List<String> enrollmentIds) {
        // Create attendance records
        boolean recA = attendanceRepo.create(sessionIds.get(0), enrollmentIds.get(0), "present");
        System.out.println("Record attendance A result = " + recA);

        // Fetch by session id
        List<Attendance> attendanceBySession = attendanceRepo.fetchAttendenceBySessionId(sessionIds.get(0));
        System.out.println("Fetch by session id count = " + attendanceBySession.size());
        dumpAttendances("Attendance records for session " + sessionIds.get(0), attendanceBySession);

        // Fetch by enrollment id
        List<Attendance> attendanceByEnrollment = attendanceRepo.fetchAttendenceByEnrollmentId(enrollmentIds.get(0));
        System.out.println("Fetch by enrollment id count = " + attendanceByEnrollment.size());
        dumpAttendances("Attendance records for enrollment " + enrollmentIds.get(0), attendanceByEnrollment);

        // Update status
        boolean updA = attendanceRepo.update(sessionIds.get(0), enrollmentIds.get(0), "absent", Timestamp.valueOf(LocalDateTime.now()));
        System.out.println("Update attendance record A result = " + updA);

        // Fetch again to verify update
        List<Attendance> updatedAttendance = attendanceRepo.fetchAttendenceBySessionId(sessionIds.get(0));
        dumpAttendances("Updated attendance records for session " + sessionIds.get(0), updatedAttendance);

    }

    private static void dumpAttendances(String title, List<Attendance> attendances) {
        System.out.println("=== " + title + " ===");
        for (Attendance a : attendances) {
            System.out.println(a.sessionId() + " | " + a.enrollmentId() + " | " + a.status() + " | Recorded at: " + a.recordedTimestamp());
        }
    }


    /**
     * Remove all rows from every table (FK-safe order).
     */
    public static void clearAllTables(DSLContext dsl) {
        dsl.transaction(cfg -> {
            DSLContext ctx = DSL.using(cfg);
            // Children first
            ctx.delete(DSL.table("attendance")).execute();
            ctx.delete(DSL.table("user_face_images")).execute();
            ctx.delete(DSL.table("student_face_images")).execute();
            ctx.delete(DSL.table("enrollments")).execute();
            ctx.delete(DSL.table("sessions")).execute();
            // Parents next
            ctx.delete(DSL.table("module_sections")).execute();
            ctx.delete(DSL.table("students")).execute();
            ctx.delete(DSL.table("users")).execute();
        });
    }
}
