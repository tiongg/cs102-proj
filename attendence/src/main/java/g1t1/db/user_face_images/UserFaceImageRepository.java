package g1t1.db.user_face_images;

import java.util.List;

public interface UserFaceImageRepository {
    String create(String userId, byte[] imageData);
    List<UserFaceImage> fetchFaceImagesByUserId(String userId);
    boolean deleteFaceImagesByUserId(String userId);
}
