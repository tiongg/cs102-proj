package g1t1.features.attendencetaking;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import g1t1.features.authentication.AuthenticationContext;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.ModuleSection;
import g1t1.models.sessions.SessionStatus;
import g1t1.opencv.FaceRecognitionService;
import g1t1.opencv.models.Recognisable;

public class AttendanceTaker {
    private static ClassSession currentSession;

    public static void start(ModuleSection moduleSection, int week, LocalDateTime startTime) {
        List<Recognisable> recognisableList = new ArrayList<>(moduleSection.getStudents());
        recognisableList.add(AuthenticationContext.getCurrentUser());

        FaceRecognitionService.getInstance().start(recognisableList);
        currentSession = new ClassSession(moduleSection, week, startTime, SessionStatus.Active);
    }

    public static void stop() {
        FaceRecognitionService.getInstance().stop();
        currentSession.endSession();
        // Save to db first!!

        currentSession = null;
    }

    public static ClassSession getCurrentSession() {
        return currentSession;
    }
}
