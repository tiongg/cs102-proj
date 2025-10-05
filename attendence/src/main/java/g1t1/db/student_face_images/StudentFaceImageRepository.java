package g1t1.db.student_face_images;

import java.util.List;

public interface StudentFaceImageRepository {
    String create(String studentId, byte[] imageData);
    List<StudentFaceImage> fetchFaceImagesByStudentId(String studentId);
    boolean deleteFaceImagesByStudentId(String studentId);
}
