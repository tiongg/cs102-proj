package g1t1.opencv.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Global event emitter for face recognition events.
 * Thread-safe event system that can be accessed from anywhere in the system.
 * Multiple components can listen to the same events.
 */
public class EventEmitter {
    private final List<FaceEventListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Add event listener from anywhere in the system.
     */
    public void addListener(FaceEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove event listener.
     */
    public void removeListener(FaceEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Emit student detected event to all listeners.
     */
    public void emitStudentDetected(StudentDetectedEvent event) {
        for (FaceEventListener listener : listeners) {
            try {
                listener.onStudentDetected(event);
            } catch (Exception e) {
                System.err.println("Error notifying event listener: " + e.getMessage());
            }
        }
    }

    /**
     * Emit attendance session event to all listeners.
     */
    public void emitAttendanceSessionEvent(AttendanceSessionEvent event) {
        for (FaceEventListener listener : listeners) {
            try {
                listener.onAttendanceSessionUpdated(event);
            } catch (Exception e) {
                System.err.println("Error notifying event listener: " + e.getMessage());
            }
        }
    }

    /**
     * Get number of registered listeners.
     */
    public int getListenerCount() {
        return listeners.size();
    }
}