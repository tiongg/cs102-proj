package g1t1.opencv.services;

import org.opencv.core.*;
import org.opencv.imgproc.*;

/**
 * Detects if a person is wearing a face mask by analyzing lower face region.
 */
public class MaskDetector {

    public boolean detectMask(Mat faceRegion) {
        if (faceRegion == null || faceRegion.empty()) {
            return false;
        }

        Mat lowerFace = extractLowerFaceRegion(faceRegion);

        Mat hsvFace = new Mat();
        if (lowerFace.channels() == 3) {
            Imgproc.cvtColor(lowerFace, hsvFace, Imgproc.COLOR_BGR2HSV);
        } else {
            Imgproc.cvtColor(lowerFace, hsvFace, Imgproc.COLOR_GRAY2BGR);
            Imgproc.cvtColor(hsvFace, hsvFace, Imgproc.COLOR_BGR2HSV);
        }

        boolean hasUniformColor = checkColorUniformity(hsvFace);
        boolean hasMaskEdges = checkMaskEdges(lowerFace);

        lowerFace.release();
        hsvFace.release();

        return hasUniformColor && hasMaskEdges;
    }

    private Mat extractLowerFaceRegion(Mat face) {
        int startY = (int) (face.height() * 0.6);
        int height = face.height() - startY;
        Rect lowerRegion = new Rect(0, startY, face.width(), height);
        return new Mat(face, lowerRegion);
    }

    private boolean checkColorUniformity(Mat hsvFace) {
        Mat hueChannel = new Mat();
        Core.extractChannel(hsvFace, hueChannel, 0);

        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();
        Core.meanStdDev(hueChannel, mean, stddev);

        double hueVariation = stddev.get(0, 0)[0];
        hueChannel.release();

        return hueVariation < 15.0;
    }

    private boolean checkMaskEdges(Mat lowerFace) {
        Mat grayFace = new Mat();
        if (lowerFace.channels() == 3) {
            Imgproc.cvtColor(lowerFace, grayFace, Imgproc.COLOR_BGR2GRAY);
        } else {
            lowerFace.copyTo(grayFace);
        }

        Mat edges = new Mat();
        Imgproc.Canny(grayFace, edges, 50, 150);

        int totalPixels = edges.rows() * edges.cols();
        int edgePixels = Core.countNonZero(edges);
        double edgeRatio = (double) edgePixels / totalPixels;

        grayFace.release();
        edges.release();

        return edgeRatio > 0.1;
    }
}