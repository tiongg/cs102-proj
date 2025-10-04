package g1t1.utils.events.opencv;

import g1t1.models.users.Student;

/**
 * Event data for student detection.
 */
public class StudentDetectedEvent {
    private final Student student;
    private final double confidence;
    private final long timestamp;

    public StudentDetectedEvent(Student student, double confidence) {
        this.student = student;
        this.confidence = confidence;
        this.timestamp = System.currentTimeMillis();
    }

    public Student getStudent() {
        return student;
    }

    public double getConfidence() {
        return confidence;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("StudentDetectedEvent{student=%s, confidence=%.1f%%}",
                student.getName(), confidence);
    }
}