package g1t1.opencv.models;

/**
 * Result of face recognition matching a detected face to a student.
 * Contains essential data for recognition pipeline.
 */
public class RecognitionResult {
    private Recognisable matchedObject;
    private double confidence;
    private DetectedFace detection;

    public RecognitionResult() {
    }

    public RecognitionResult(Recognisable matchedObject, double confidence, DetectedFace detection) {
        this.matchedObject = matchedObject;
        this.confidence = confidence;
        this.detection = detection;
    }

    public Recognisable getMatchedObject() {
        return this.matchedObject;
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
                this.matchedObject != null ? this.matchedObject.getName() : "null",
                confidence,
                detection != null ? detection.toString() : "null");
    }
}