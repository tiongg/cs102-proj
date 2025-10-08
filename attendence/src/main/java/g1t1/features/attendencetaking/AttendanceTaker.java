package g1t1.features.attendencetaking;

import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.ModuleSection;
import g1t1.models.sessions.SessionStatus;
import g1t1.opencv.FaceRecognitionService;

import java.time.LocalDateTime;

public class AttendanceTaker {
    private static ClassSession currentSession;

    public static void start(ModuleSection moduleSection, int week) {
        FaceRecognitionService.getInstance().start(moduleSection.getStudents());
        currentSession = new ClassSession(moduleSection, week, LocalDateTime.now(), SessionStatus.Active);
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
