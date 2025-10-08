package g1t1.features.authentication;

import g1t1.db.DSLInstance;
import g1t1.db.user_face_images.UserFaceImage;
import g1t1.db.user_face_images.UserFaceImageRepository;
import g1t1.db.user_face_images.UserFaceImageRepositoryJooq;
import g1t1.db.users.User;
import g1t1.db.users.UserRepository;
import g1t1.db.users.UserRepositoryJooq;
import g1t1.models.users.RegisterTeacher;
import g1t1.models.users.Teacher;
import g1t1.utils.EventEmitter;
import g1t1.utils.events.authentication.OnLoginEvent;
import g1t1.utils.events.authentication.OnLogoutEvent;
import org.jooq.exception.DataAccessException;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
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

            User dbUser = userRepo.fetchUserByEmail(email).orElse(userRepo.fetchUserById(email).orElse(null));
            if (dbUser == null) {
                return false;
            }
            if (!BCrypt.checkpw(password, dbUser.passwordHash())) {
                return false;
            }
            List<UserFaceImage> dbFaces = userFaceImageRepo.fetchFaceImagesByUserId(dbUser.userId());
            Teacher teacher = new Teacher(dbUser, dbFaces);
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

    private static boolean setCurrentTeacher(Teacher teacher) {
        // Auth/Registration failed
        if (teacher == null) {
            return false;
        }

        emitter.emit(new OnLoginEvent(teacher));
        currentUser = teacher;
        return true;
    }
}
