package g1t1.models.users;
import java.util.List;

/**
 * Face data for recognition.
 * Stores raw face images for OpenCV processing.
 */
public class FaceData {
    private List<byte[]> faceImages; // JPEG-encoded face images

    public FaceData() {
    }

    public List<byte[]> getFaceImages() {
        return faceImages;
    }

    public void setFaceImages(List<byte[]> faceImages) {
        this.faceImages = faceImages;
    }
}
