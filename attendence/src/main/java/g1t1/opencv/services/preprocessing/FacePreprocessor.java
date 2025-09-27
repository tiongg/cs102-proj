package g1t1.opencv.services.preprocessing;

import org.opencv.core.Mat;

/**
 * Strategy interface for face preprocessing steps.
 */
public interface FacePreprocessor {
    Mat process(Mat face);
}