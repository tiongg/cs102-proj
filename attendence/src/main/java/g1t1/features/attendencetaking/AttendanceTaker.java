package g1t1.features.attendencetaking;

import g1t1.components.Toast;
import g1t1.config.SettingsManager;
import g1t1.db.DSLInstance;
import g1t1.db.attendance.AttendanceRepository;
import g1t1.db.attendance.AttendanceRepositoryJooq;
import g1t1.db.attendance.AttendanceStatus;
import g1t1.db.attendance.MarkingMethod;
import g1t1.db.sessions.SessionRepository;
import g1t1.db.sessions.SessionRepositoryJooq;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.features.logger.AppLogger;
import g1t1.models.ids.StudentID;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.ModuleSection;
import g1t1.models.sessions.SessionAttendance;
import g1t1.models.sessions.SessionStatus;
import g1t1.opencv.FaceRecognitionService;
import g1t1.opencv.models.Recognisable;
import g1t1.utils.events.opencv.StudentDetectedEvent;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import org.jooq.exception.DataAccessException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AttendanceTaker {
    public static final ListProperty<SessionAttendance> recentlyMarked =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    public static final ObjectProperty<StudentDetectedEvent> needsConfirmation =
            new SimpleObjectProperty<>();
    public static final IntegerProperty studentsPresent = new SimpleIntegerProperty();

    private static final int MAX_RECENTLY_DISPLAYED = 3;

    private static ClassSession currentSession;

    public static void start(ModuleSection moduleSection, int week, LocalDateTime startTime) {
        initializeRecognitionSystem(moduleSection);
        initializeSession(moduleSection, week, startTime);
        subscribeToDetectionEvents();
    }

    public static void stop() {
        AppLogger.log("Stopped attendance taking");
        FaceRecognitionService.getInstance().stop();

        if (currentSession != null) {
            currentSession.endSession();
            saveSessionToDb();
        }

        currentSession = null;
    }

    public static ClassSession getCurrentSession() {
        return currentSession;
    }

    public static boolean acceptPrompt() {
        StudentDetectedEvent event = needsConfirmation.getValue();

        if (event == null) {
            return false;
        }
        SessionAttendance attendance = getAttendanceRecord(event.getStudent().getId());
        if (attendance == null) {
            return false;
        }

        markAttendance(
                attendance,
                currentSession.getCurrentStatus(),
                event.getConfidence(),
                MarkingMethod.AFTER_CONFIRMATION
        );

        needsConfirmation.set(null);
        return true;
    }

    public static void rejectPrompt() {
        needsConfirmation.set(null);
    }

    private static void initializeRecognitionSystem(ModuleSection moduleSection) {
        List<Recognisable> recognisableList = new ArrayList<>(moduleSection.getStudents());
        recognisableList.add(AuthenticationContext.getCurrentUser());

        FaceRecognitionService.getInstance().start(recognisableList);
    }

    private static void initializeSession(ModuleSection moduleSection, int week, LocalDateTime startTime) {
        currentSession = new ClassSession(moduleSection, week, startTime, SessionStatus.Active);
        recentlyMarked.clear();
    }

    private static void subscribeToDetectionEvents() {
        FaceRecognitionService.getInstance()
                .getEventEmitter()
                .subscribe(StudentDetectedEvent.class, AttendanceTaker::handleDetectionEvent);
    }

    private static void handleDetectionEvent(StudentDetectedEvent event) {
        Platform.runLater(() -> processDetection(event));
    }

    private static void processDetection(StudentDetectedEvent event) {
        SessionAttendance attendance = getAttendanceRecord(event.getStudent().getId());
        if (attendance == null) {
            return;
        }

        attendance.updateBestConfidence(event.getConfidence());

        // Skip if already marked
        if (attendance.getStatus() != AttendanceStatus.PENDING) {
            return;
        }

        // Process based on confidence level
        if (event.getConfidence() >= SettingsManager.getInstance().getSettings().getAutoMarkThreshold()) {
            processAutoMarking(event, attendance);
        } else {
            requestManualConfirmation(event);
        }
    }

    private static void processAutoMarking(StudentDetectedEvent event, SessionAttendance attendance) {
        AppLogger.log(String.format("Auto marked %s", attendance.getStudent().getName()));
        markAttendance(
                attendance,
                currentSession.getCurrentStatus(),
                event.getConfidence(),
                MarkingMethod.AUTOMATIC
        );

        clearConfirmationIfMatches(event.getStudent().getId());

        showSuccessToast(event.getStudent().getName());
    }

    private static void markAttendance(
            SessionAttendance attendance,
            AttendanceStatus status,
            double confidence,
            MarkingMethod method
    ) {
        attendance.setStatus(status, confidence, method);
        addToRecentlyMarked(attendance);
        studentsPresent.set(currentSession
                .getStudentAttendance()
                .values()
                .stream()
                .filter(x -> x.getStatus() == AttendanceStatus.PRESENT || x.getStatus() == AttendanceStatus.LATE)
                .toList()
                .size()
        );
    }

    private static void addToRecentlyMarked(SessionAttendance attendance) {
        recentlyMarked.add(attendance);

        // Maintain size limit
        if (recentlyMarked.size() > MAX_RECENTLY_DISPLAYED) {
            recentlyMarked.removeFirst();
        }
    }

    private static void clearConfirmationIfMatches(StudentID studentID) {
        StudentDetectedEvent pendingEvent = needsConfirmation.getValue();

        if (pendingEvent != null && studentID.equals(pendingEvent.getStudent().getId())) {
            needsConfirmation.set(null);
        }
    }

    private static void requestManualConfirmation(StudentDetectedEvent event) {
        needsConfirmation.set(event);
    }

    private static void showSuccessToast(String studentName) {
        String message = String.format("Marked attendance for %s!", studentName);
        Toast.show(message, 3000, Toast.ToastType.SUCCESS);
    }

    private static SessionAttendance getAttendanceRecord(StudentID studentID) {
        if (currentSession == null) {
            return null;
        }
        return currentSession.getStudentAttendance().get(studentID);
    }

    private static void saveSessionToDb() {
        try (DSLInstance dslInstance = new DSLInstance()) {
            SessionRepository sessionRepo = new SessionRepositoryJooq(dslInstance.dsl);
            AttendanceRepository attendanceRepo = new AttendanceRepositoryJooq(dslInstance.dsl);

            ClassSession currentSession = getCurrentSession();

            String sessionId = sessionRepo.create(currentSession);
            attendanceRepo.createAll(sessionId, currentSession);

            AuthenticationContext.getCurrentUser().getPastSessions().add(currentSession);
            AuthenticationContext.triggerUserUpdate();
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
        } catch (DataAccessException e) {
            System.out.println("Error during database operation: " + e.getMessage());
        }
    }
}