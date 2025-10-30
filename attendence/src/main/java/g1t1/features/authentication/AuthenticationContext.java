package g1t1.features.authentication;

import g1t1.db.DSLInstance;
import g1t1.db.DbUtils;
import g1t1.db.attendance.AttendanceRecord;
import g1t1.db.attendance.AttendanceRepository;
import g1t1.db.attendance.AttendanceRepositoryJooq;
import g1t1.db.enrollments.Enrollment;
import g1t1.db.enrollments.EnrollmentRepository;
import g1t1.db.enrollments.EnrollmentRepositoryJooq;
import g1t1.db.module_sections.ModuleSectionRecord;
import g1t1.db.module_sections.ModuleSectionRepository;
import g1t1.db.module_sections.ModuleSectionRepositoryJooq;
import g1t1.db.sessions.SessionRecord;
import g1t1.db.sessions.SessionRepository;
import g1t1.db.sessions.SessionRepositoryJooq;
import g1t1.db.user_face_images.UserFaceImage;
import g1t1.db.user_face_images.UserFaceImageRepository;
import g1t1.db.user_face_images.UserFaceImageRepositoryJooq;
import g1t1.db.users.User;
import g1t1.db.users.UserRepository;
import g1t1.db.users.UserRepositoryJooq;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.ModuleSection;
import g1t1.models.users.RegisterTeacher;
import g1t1.models.users.Student;
import g1t1.models.users.Teacher;
import g1t1.utils.EventEmitter;
import g1t1.utils.events.authentication.OnLoginEvent;
import g1t1.utils.events.authentication.OnLogoutEvent;
import g1t1.utils.events.authentication.OnUserUpdateEvent;
import org.jooq.exception.DataAccessException;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class AuthenticationContext {
    public static final EventEmitter<Object> emitter = new EventEmitter<>();

    private static Teacher currentUser;

    public static boolean registerTeacher(RegisterTeacher registrationInfo) {
        try (DSLInstance dslInstance = new DSLInstance()) {
            UserRepository userRepo = new UserRepositoryJooq(dslInstance.dsl);
            UserFaceImageRepository userFaceImageRepo = new UserFaceImageRepositoryJooq(dslInstance.dsl);
            String hashed = BCrypt.hashpw(registrationInfo.getPassword(), BCrypt.gensalt());

            String userId = userRepo.create(registrationInfo.getTeacherID().toString(),
                    registrationInfo.getFullName(), registrationInfo.getEmail(), hashed);
            for (byte[] faceImage : registrationInfo.getFaceData().getFaceImages()) {
                userFaceImageRepo.create(userId, faceImage);
            }

            // Login the user
            return loginTeacher(registrationInfo.getEmail(), registrationInfo.getPassword());
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
        } catch (DataAccessException e) {
            System.out.println("Error during database operation: " + e.getMessage());
        }
        return false;
    }

    public static boolean loginTeacher(String email, String password) {
        try (DSLInstance dslInstance = new DSLInstance()) {
            UserRepository userRepo = new UserRepositoryJooq(dslInstance.dsl);
            UserFaceImageRepository userFaceImageRepo = new UserFaceImageRepositoryJooq(dslInstance.dsl);
            ModuleSectionRepository moduleSectionRepository = new ModuleSectionRepositoryJooq(dslInstance.dsl);
            EnrollmentRepository enrollmentRepository = new EnrollmentRepositoryJooq(dslInstance.dsl);
            SessionRepository sessionRepository = new SessionRepositoryJooq(dslInstance.dsl);
            AttendanceRepository attendanceRepository = new AttendanceRepositoryJooq(dslInstance.dsl);

            User dbUser = userRepo.fetchUserByEmail(email).orElse(userRepo.fetchUserById(email).orElse(null));
            if (dbUser == null) {
                return false;
            }
            if (!BCrypt.checkpw(password, dbUser.passwordHash())) {
                return false;
            }
            String currentTerm = ModuleSection.getCurrentAYTerm();

            List<UserFaceImage> dbFaces = userFaceImageRepo.fetchFaceImagesByUserId(dbUser.userId());
            List<ModuleSectionRecord> dbSections = moduleSectionRepository
                    .fetchModuleSectionsByTeacherIdAndTerm(dbUser.userId(), currentTerm);

            Teacher teacher = new Teacher(dbUser, dbFaces);

            for (ModuleSectionRecord dbSection : dbSections) {
                ModuleSection section = new ModuleSection(dbSection);

                // TODO: This should be a join
                HashMap<String, Student> enrollmentToStudent = new HashMap<>();
                List<Enrollment> enrollments = enrollmentRepository
                        .fetchEnrollmentsByModuleSectionId(dbSection.moduleSectionId())
                        .stream()
                        .toList();
                for (Enrollment enrollment : enrollments) {
                    Student student = DbUtils.getStudentById(enrollment.studentId(), section);
                    section.addStudent(student);
                    enrollmentToStudent.put(enrollment.enrollmentId(), student);
                }

                List<SessionRecord> sessions = sessionRepository.fetchSessionsByModuleSectionId(section.getId());
                for (SessionRecord dbSession : sessions) {
                    String sessionId = dbSession.sessionId();
                    List<AttendanceRecord> dbAttendances = attendanceRepository.fetchAttendenceBySessionId(sessionId);
                    ClassSession session = new ClassSession(dbSession, section, dbAttendances, enrollmentToStudent);

                    teacher.getPastSessions().add(session);
                }

                teacher.getModuleSections().add(section);
            }

            return setCurrentTeacher(teacher);
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
        } catch (DataAccessException e) {
            System.out.println("Error during database operation: " + e.getMessage());
        }
        return false;
    }

    public static void logout() {
        currentUser = null;
        emitter.emit(new OnLogoutEvent());
    }

    public static Teacher getCurrentUser() {
        return currentUser;
    }

    public static void triggerUserUpdate() {
        emitter.emit(new OnUserUpdateEvent(currentUser));
    }

    private static boolean setCurrentTeacher(Teacher teacher) {
        // Auth/Registration failed
        if (teacher == null) {
            return false;
        }

        currentUser = teacher;
        emitter.emit(new OnLoginEvent(teacher));
        triggerUserUpdate();
        return true;
    }
}
