package g1t1.opencv.models;

import g1t1.models.users.Student;

/**
 * Individual student attendance record within the session.
 */
public class StudentAttendanceRecord {
    private final Student student;
    private double maxConfidence;
    private long firstDetectionTime;
    private long lastDetectionTime;
    private int detectionCount;

    public StudentAttendanceRecord(Student student, double initialConfidence, long detectionTime) {
        this.student = student;
        this.maxConfidence = initialConfidence;
        this.firstDetectionTime = detectionTime;
        this.lastDetectionTime = detectionTime;
        this.detectionCount = 1;
    }

    public void updateMaxConfidence(double confidence, long detectionTime) {
        if (confidence > this.maxConfidence) {
            this.maxConfidence = confidence;
        }
        this.lastDetectionTime = detectionTime;
    }

    public void incrementDetectionCount() {
        this.detectionCount++;
    }

    public Student getStudent() {
        return student;
    }

    public double getMaxConfidence() {
        return maxConfidence;
    }

    public long getFirstDetectionTime() {
        return firstDetectionTime;
    }

    public long getLastDetectionTime() {
        return lastDetectionTime;
    }

    public int getDetectionCount() {
        return detectionCount;
    }

    @Override
    public String toString() {
        return String.format("StudentRecord{id=%s, name=%s, maxConfidence=%.1f%%, detections=%d}",
                student.getId(), student.getName(), maxConfidence, detectionCount);
    }
}
