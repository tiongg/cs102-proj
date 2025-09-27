package g1t1.opencv.models;

import org.opencv.core.Rect;

/**
 * Model for a single detected face.
 * Contains essential data for face detection and tracking.
 */
public class DetectedFace {
    private Rect boundingBox;
    private double confidence;
    private int faceId;
    private long timestamp;

    public DetectedFace() {
        this.timestamp = System.currentTimeMillis();
    }

    public DetectedFace(Rect boundingBox, double confidence, int faceId) {
        this();
        this.boundingBox = boundingBox;
        this.confidence = confidence;
        this.faceId = faceId;
    }

    public Rect getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(Rect boundingBox) {
        this.boundingBox = boundingBox;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public int getFaceId() {
        return faceId;
    }

    public void setFaceId(int faceId) {
        this.faceId = faceId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("DetectedFace{id=%d, confidence=%.1f%%, box=[%d,%d,%dx%d]}",
                faceId, confidence,
                boundingBox != null ? boundingBox.x : 0,
                boundingBox != null ? boundingBox.y : 0,
                boundingBox != null ? boundingBox.width : 0,
                boundingBox != null ? boundingBox.height : 0);
    }
}