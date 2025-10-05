package g1t1.db.user_face_images;

public record UserFaceImage (
    String faceImageId,
    String userId,
    byte[] imageData
) {}
