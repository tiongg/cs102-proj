package g1t1.opencv.models;

import g1t1.models.users.Student;

/**
 * Result of face recognition matching a detected face to a student.
 * Contains essential data for recognition pipeline.
 */
public class RecognitionResult {
    private Student matchedStudent;
    private double confidence;
    private DetectedFace detection;

    public RecognitionResult() {
    }

    public RecognitionResult(Student matchedStudent, double confidence, DetectedFace detection) {
        this.matchedStudent = matchedStudent;
        this.confidence = confidence;
        this.detection = detection;
    }

    public Student getMatchedStudent() {
        return matchedStudent;
    }

    public void setMatchedStudent(Student matchedStudent) {
        this.matchedStudent = matchedStudent;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public DetectedFace getDetection() {
        return detection;
    }

    public void setDetection(DetectedFace detection) {
        this.detection = detection;
    }

    @Override
    public String toString() {
        return String.format("RecognitionResult{student=%s, confidence=%.1f%%, detection=%s}",
                matchedStudent != null ? matchedStudent.getName() : "null",
                confidence,
                detection != null ? detection.toString() : "null");
    }
}