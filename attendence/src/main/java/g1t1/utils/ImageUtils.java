package g1t1.utils;

import javafx.scene.image.Image;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;

public class ImageUtils {
    public static Image matToImage(Mat frame) {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer
        Imgcodecs.imencode(".png", frame, buffer);
        // build and return an Image created from the image encoded in the buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    public static Image bytesToImage(byte[] frame) {
        return new Image(new ByteArrayInputStream(frame));
    }

    public static Mat cropToFit(Mat source, int targetWidth, int targetHeight) {
        int height = source.rows();
        int width = source.cols();

        double targetAspect = (double) targetWidth / targetHeight;
        double currentAspect = (double) width / height;

        Mat result = new Mat();

        // Same aspect ratio, just resize
        if (Math.abs(targetAspect - currentAspect) < 0.01) {
            Imgproc.resize(source, result, new Size(targetWidth, targetHeight));
            return result;
        }

        if (currentAspect > targetAspect) {
            // Current image is wider - crop the width
            int cropWidth = (int) (height * targetAspect);
            int startX = (width - cropWidth) / 2;
            Rect cropRect = new Rect(startX, 0, cropWidth, height);
            Mat cropped = new Mat(source, cropRect);
            Imgproc.resize(cropped, result, new Size(targetWidth, targetHeight));
        } else {
            // Current image is taller - crop the height
            int cropHeight = (int) (width / targetAspect);
            int startY = (height - cropHeight) / 2;
            Rect cropRect = new Rect(0, startY, width, cropHeight);
            Mat cropped = new Mat(source, cropRect);
            Imgproc.resize(cropped, result, new Size(targetWidth, targetHeight));
        }

        return result;
    }

    public static Rect centerRect(int width, int height, int parentWidth, int parentHeight) {
        return new Rect(parentWidth / 2 - width / 2, parentHeight / 2 - height / 2, width, height);
    }
}
