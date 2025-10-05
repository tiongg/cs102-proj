package g1t1.features.authentication;

import g1t1.models.users.RegisterTeacher;
import g1t1.models.users.Teacher;
import g1t1.testing.MockDb;
import g1t1.utils.EventEmitter;
import g1t1.utils.events.authentication.OnLoginEvent;
import g1t1.utils.events.authentication.OnLogoutEvent;

public class AuthenticationContext {
    public static final EventEmitter<Object> emitter = new EventEmitter<>();

    private static Teacher currentUser;

    public static boolean registerTeacher(RegisterTeacher registrationInfo) {
        Teacher teacher = MockDb.registerTeacher(registrationInfo);
        return setCurrentTeacher(teacher);
    }

    public static boolean loginTeacher(String email, String password) {
        Teacher teacher = MockDb.loginUser(email, password);
        return setCurrentTeacher(teacher);
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
