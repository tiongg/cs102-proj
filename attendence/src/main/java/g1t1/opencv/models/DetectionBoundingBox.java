package g1t1.opencv.models;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class DetectionBoundingBox {
    private final Point p1;
    private final Point p2;
    private final Scalar color;
    private final int thickness;
    private String name;
    private String livenessInfo;
    private double confidence;
    private boolean isPicture = false;

    public DetectionBoundingBox(Point p1, Point p2, Scalar color, int thickness) {
        this.p1 = p1;
        this.p2 = p2;
        this.color = color;
        this.thickness = thickness;
    }

    public DetectionBoundingBox(Point p1, Point p2, Scalar color, int thickness, String name, String livenessInfo, double confidence) {
        this(p1, p2, color, thickness);
        this.name = name;
        this.livenessInfo = livenessInfo;
        this.confidence = confidence;
    }

    public DetectionBoundingBox(Point p1, Point p2, Scalar color, int thickness, boolean isPicture) {
        this(p1, p2, color, thickness);
        this.isPicture = isPicture;
    }

    public void drawOnFrame(Mat frame) {
        Imgproc.rectangle(frame, this.p1, this.p2, this.color, this.thickness);

        // Its a picture
        if (this.isPicture) {
            Imgproc.putText(frame, "PHOTO DETECTED",
                    new Point(p1.x, p1.y - 10),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.6,
                    new Scalar(0, 0, 255), 2);
        }

        // If name data, display it too
        if (this.name != null) {
            String nameLabel = this.name +
                    " (" + String.format("%.1f%%", confidence) + ")" + livenessInfo;

            Imgproc.putText(frame, nameLabel,
                    new Point(p1.x, p1.y - 10),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.6,
                    new Scalar(255, 0, 0), 2);
        }
    }
}
