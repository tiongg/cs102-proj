package g1t1.features.attendencetaking;

import g1t1.components.Toast;
import g1t1.db.attendance.AttendanceStatus;
import g1t1.db.attendance.MarkingMethod;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.models.ids.StudentID;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.ModuleSection;
import g1t1.models.sessions.SessionAttendance;
import g1t1.models.sessions.SessionStatus;
import g1t1.models.users.Student;
import g1t1.opencv.FaceRecognitionService;
import g1t1.opencv.models.Recognisable;
import g1t1.utils.events.opencv.StudentDetectedEvent;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AttendanceTaker {
    public static final ListProperty<SessionAttendance> recentlyMarked = new SimpleListProperty<>(FXCollections.observableArrayList());
    
    private static final int MAX_RECENTLY_DISPLAYED = 3;
    private static final double PROMPT_THRESHOLD = 20;  // 20% - 40% above will be prompted for confirmation
    private static final double AUTO_THRESHOLD = 40; // 40% and above will be auto marked
    private static ClassSession currentSession;

    public static void start(ModuleSection moduleSection, int week, LocalDateTime startTime) {
        List<Recognisable> recognisableList = new ArrayList<>(moduleSection.getStudents());
        recognisableList.add(AuthenticationContext.getCurrentUser());

        FaceRecognitionService.getInstance().start(recognisableList);
        currentSession = new ClassSession(moduleSection, week, startTime, SessionStatus.Active);
        recentlyMarked.clear();

        FaceRecognitionService.getInstance().getEventEmitter().subscribe(
                StudentDetectedEvent.class,
                (e) -> {
                    if (e.getConfidence() < PROMPT_THRESHOLD) {
                        return;
                    }

                    Student detectedStudent = e.getStudent();
                    SessionAttendance sessionAttendance = currentSession.getStudentAttendance().get(detectedStudent.getId());
                    if (sessionAttendance == null) {
                        return;
                    }

                    sessionAttendance.updateBestConfidence(e.getConfidence());
                    // Marked already
                    if (sessionAttendance.getStatus() != AttendanceStatus.PENDING) {
                        return;
                    }

                    if (e.getConfidence() >= AUTO_THRESHOLD) {
                        sessionAttendance.setStatus(currentSession.getCurrentStatus(), e.getConfidence(), MarkingMethod.AUTOMATIC);
                        recentlyMarked.add(sessionAttendance);
                        if (recentlyMarked.size() > MAX_RECENTLY_DISPLAYED) {
                            recentlyMarked.removeFirst();
                        }
                        Toast.show(String.format("Marked attendance for %s!", detectedStudent.getName()), 3000, Toast.ToastType.SUCCESS);
                    }
                }
        );
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

    public static boolean manualOverride(StudentID studentID, AttendanceStatus status) {
        SessionAttendance sessionAttendance = currentSession.getStudentAttendance().get(studentID);
        if (sessionAttendance == null) {
            return false;
        }
        sessionAttendance.setStatus(status, 1, MarkingMethod.MANUAL);
        return true;
    }

    public static boolean confirmMarking(StudentID studentID) {
        SessionAttendance sessionAttendance = currentSession.getStudentAttendance().get(studentID);
        if (sessionAttendance == null) {
            return false;
        }

        return true;
    }
}
