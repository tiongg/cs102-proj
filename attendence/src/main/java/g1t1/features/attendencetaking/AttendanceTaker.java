package g1t1.features.attendencetaking;

import g1t1.models.sessions.ModuleSection;
import g1t1.opencv.FaceRecognitionService;

public class AttendanceTaker {
    public static void start(ModuleSection moduleSection) {
        FaceRecognitionService.getInstance().start(moduleSection.getStudents());
    }

    public static void stop() {
        FaceRecognitionService.getInstance().stop();
    }
}
