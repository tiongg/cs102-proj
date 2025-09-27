package g1t1.opencv.events;

import g1t1.opencv.models.AttendanceSession;

/**
 * Event for attendance session updates that frontend can listen to.
 * Provides complete session state for real-time attendance tracking.
 */
public class AttendanceSessionEvent {
    private final AttendanceSession session;
    private final String eventType;
    private final long timestamp;

    public AttendanceSessionEvent(AttendanceSession session, String eventType) {
        this.session = session;
        this.eventType = eventType;
        this.timestamp = System.currentTimeMillis();
    }

    public AttendanceSession getSession() {
        return session;
    }

    public String getEventType() {
        return eventType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("AttendanceSessionEvent{type=%s, studentsDetected=%d}",
                eventType, session.getTotalStudentsDetected());
    }

    // Event types
    public static final String SESSION_STARTED = "SESSION_STARTED";
    public static final String STUDENT_UPDATED = "STUDENT_UPDATED";
    public static final String SESSION_ENDED = "SESSION_ENDED";
}