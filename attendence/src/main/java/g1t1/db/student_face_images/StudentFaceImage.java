package g1t1.db.student_face_images;

public record StudentFaceImage(
    String faceImageId, 
    String studentId, 
    byte[] imageData
) {}   

