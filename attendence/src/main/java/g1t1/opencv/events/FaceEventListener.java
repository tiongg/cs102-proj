package g1t1.opencv.events;

/**
 * Event listener interface for face recognition events.
 * Any part of the system can implement this to listen to face recognition events.
 */
public interface FaceEventListener {

    /**
     * Called when a student is detected and recognized.
     */
    void onStudentDetected(StudentDetectedEvent event);

    /**
     * Called when attendance session is updated with new detection data.
     * Default implementation does nothing for backward compatibility.
     */
    default void onAttendanceSessionUpdated(AttendanceSessionEvent event) {
        // Default implementation - can be overridden
    }
}