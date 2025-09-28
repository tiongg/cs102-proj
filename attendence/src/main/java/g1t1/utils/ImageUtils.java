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

    public static Mat cropToSquare(Mat source, int targetSize) {
        int height = source.rows();
        int width = source.cols();

        Mat result = new Mat();

        if (width == height) {
            // Already square, just resize if needed
            if (width != targetSize) {
                Imgproc.resize(source, result, new Size(targetSize, targetSize));
            } else {
                result = source.clone();
            }
        } else if (width > height) {
            // Landscape: crop width to match height
            int startX = (width - height) / 2;
            Rect cropRect = new Rect(startX, 0, height, height);
            Mat cropped = new Mat(source, cropRect);

            if (height != targetSize) {
                Imgproc.resize(cropped, result, new Size(targetSize, targetSize));
            } else {
                result = cropped.clone();
            }
        } else {
            // Portrait: crop height to match width
            int startY = (height - width) / 2;
            Rect cropRect = new Rect(0, startY, width, width);
            Mat cropped = new Mat(source, cropRect);

            if (width != targetSize) {
                Imgproc.resize(cropped, result, new org.opencv.core.Size(targetSize, targetSize));
            } else {
                result = cropped.clone();
            }
        }

        return result;
    }
}
