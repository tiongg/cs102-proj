package g1t1.config;

/**
 * POJO class representing application settings.
 * These settings can be modified via the Settings UI and are persisted to JSON.
 */
public class AppSettings {
    private int detectionThreshold;
    private int autoMarkThreshold;
    private int lateThresholdMinutes;
    private int cameraDevice;
    private String logPath;
    private boolean livenessEnabled;
    private double laplacianVarianceThreshold;
    private double textureRatioThreshold;

    /**
     * Constructor with default values
     */
    public AppSettings() {
        this.detectionThreshold = 50;
        this.autoMarkThreshold = 80;
        this.lateThresholdMinutes = 15;
        this.cameraDevice = 0;
        this.logPath = "logs/";
        this.livenessEnabled = true;
        this.laplacianVarianceThreshold = 250.0;
        this.textureRatioThreshold = 0.06;
    }

    public int getDetectionThreshold() {
        return detectionThreshold;
    }

    public void setDetectionThreshold(int detectionThreshold) {
        this.detectionThreshold = detectionThreshold;
    }

    public int getAutoMarkThreshold() {
        return autoMarkThreshold;
    }

    public void setAutoMarkThreshold(int autoMarkThreshold) {
        this.autoMarkThreshold = autoMarkThreshold;
    }

    public int getLateThresholdMinutes() {
        return lateThresholdMinutes;
    }

    public void setLateThresholdMinutes(int lateThresholdMinutes) {
        this.lateThresholdMinutes = lateThresholdMinutes;
    }

    public int getCameraDevice() {
        return cameraDevice;
    }

    public void setCameraDevice(int cameraDevice) {
        this.cameraDevice = cameraDevice;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public Boolean getLivenessEnabled() {
        return this.livenessEnabled;
    }

    public void setLivenessEnabled(boolean newVal) {
        this.livenessEnabled = newVal;
    }

    public double getLaplacianVarianceThreshold() {
        return laplacianVarianceThreshold;
    }

    public void setLaplacianVarianceThreshold(double laplacianVarianceThreshold) {
        this.laplacianVarianceThreshold = laplacianVarianceThreshold;
    }

    public double getTextureRatioThreshold() {
        return textureRatioThreshold;
    }

    public void setTextureRatioThreshold(double textureRatioThreshold) {
        this.textureRatioThreshold = textureRatioThreshold;
    }
}
