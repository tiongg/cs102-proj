package g1t1.opencv.models;

/**
 * Result of liveness detection check.
 */
public class LivenessResult {
    private final boolean isLive;
    private final double confidence;
    private final String reason;

    public LivenessResult(boolean isLive, double confidence, String reason) {
        this.isLive = isLive;
        this.confidence = confidence;
        this.reason = reason;
    }

    public boolean isLive() {
        return isLive;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getReason() {
        return reason;
    }
}