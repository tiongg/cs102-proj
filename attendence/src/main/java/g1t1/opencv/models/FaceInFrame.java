package g1t1.opencv.models;

import org.opencv.core.Mat;

public record FaceInFrame(int x, int y, Mat faceBounds, int maxWidth, int maxHeight) {
}
