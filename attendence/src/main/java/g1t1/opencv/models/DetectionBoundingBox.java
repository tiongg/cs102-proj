package g1t1.opencv.models;

import g1t1.models.users.Teacher;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class DetectionBoundingBox {
    private final Point p1;
    private final Point p2;
    private final int thickness;
    private Scalar color;
    private String name;
    private String livenessInfo;
    private double confidence;
    private boolean isPicture = false;
    private boolean isTeacher = false;

    public DetectionBoundingBox(Point p1, Point p2, int thickness) {
        this.p1 = p1;
        this.p2 = p2;
        this.color = new Scalar(0, 0, 255);
        this.thickness = thickness;
    }

    public void setRecognised(Recognisable recognizedObject, String liveness, double confidence) {
        this.name = recognizedObject.getName();
        this.livenessInfo = liveness;
        this.confidence = confidence;
        if (recognizedObject instanceof Teacher) {
            this.isTeacher = true;
            this.color = new Scalar(238, 130, 238);
        } else {
            this.color = new Scalar(0, 255, 0);
        }
    }

    public void setPicture() {
        this.isPicture = true;
        this.color = new Scalar(255, 0, 0);
    }

    public void drawOnFrame(Mat frame) {
        Imgproc.rectangle(frame, this.p1, this.p2, this.color, this.thickness);

        // Its a picture
        if (this.isPicture) {
            Imgproc.putText(frame, "PHOTO DETECTED", new Point(p1.x, p1.y - 10), Imgproc.FONT_HERSHEY_SIMPLEX, 0.6,
                    this.color, 2);
        }

        // If name data, display it too
        if (this.name != null) {
            String nameLabel = this.name + " (" + String.format("%.1f%%", confidence) + ")" + livenessInfo;

            Imgproc.putText(frame, nameLabel, new Point(p1.x, p1.y - 10), Imgproc.FONT_HERSHEY_SIMPLEX, 0.6,
                    this.color, 2);
        }
    }

    public boolean getIsTeacher() {
        return isTeacher;
    }
}
