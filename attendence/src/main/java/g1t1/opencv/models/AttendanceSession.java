package g1t1.opencv.models;

import g1t1.models.users.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks attendance session data with maximum confidence levels for each detected student.
 * Maintains running state from session start to stop.
 */
public class AttendanceSession {
    private final Map<String, StudentAttendanceRecord> attendanceRecords;
    private final long sessionStartTime;
    private boolean isActive;

    public AttendanceSession() {
        this.attendanceRecords = new ConcurrentHashMap<>();
        this.sessionStartTime = System.currentTimeMillis();
        this.isActive = true;
    }

    /**
     * Update student detection with new confidence level.
     * Keeps track of maximum confidence seen for each student.
     */
    public void updateRecognisedDetection(Recognisable recognisedObject, double confidence) {
        if (!isActive) return;
        if (!(recognisedObject instanceof Student student)) return;

        String studentId = student.getId().toString();

        attendanceRecords.compute(studentId, (id, existing) -> {
            if (existing == null) {
                return new StudentAttendanceRecord(student, confidence, System.currentTimeMillis());
            } else {
                // Update max confidence if this detection is better
                if (confidence > existing.getMaxConfidence()) {
                    existing.updateMaxConfidence(confidence, System.currentTimeMillis());
                }
                existing.incrementDetectionCount();
                return existing;
            }
        });
    }

    /**
     * Get current attendance records for all detected students.
     */
    public List<StudentAttendanceRecord> getCurrentAttendanceRecords() {
        return new ArrayList<>(attendanceRecords.values());
    }

    /**
     * Get attendance record for specific student.
     */
    public StudentAttendanceRecord getStudentRecord(String studentId) {
        return attendanceRecords.get(studentId);
    }

    /**
     * Check if student has been detected in this session.
     */
    public boolean isStudentDetected(String studentId) {
        return attendanceRecords.containsKey(studentId);
    }

    /**
     * Get maximum confidence recorded for a student.
     */
    public double getMaxConfidenceForStudent(String studentId) {
        StudentAttendanceRecord record = attendanceRecords.get(studentId);
        return record != null ? record.getMaxConfidence() : 0.0;
    }

    /**
     * Get total number of unique students detected.
     */
    public int getTotalStudentsDetected() {
        return attendanceRecords.size();
    }

    /**
     * End the session.
     */
    public void endSession() {
        this.isActive = false;
    }

    public long getSessionStartTime() {
        return sessionStartTime;
    }

    public boolean isActive() {
        return isActive;
    }
}