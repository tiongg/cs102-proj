package g1t1.db.student_face_images;

import org.jooq.impl.DSL;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.Field;

import java.util.List;
import java.util.UUID;

public class StudentFaceImageRepositoryJooq implements StudentFaceImageRepository {
    private final DSLContext dsl;

    private final Table<?> STUDENT_FACE_IMAGES_TABLE = DSL.table("student_face_images");
    private final Field<String> STUDENT_FACE_IMAGE_ID = DSL.field("student_face_image_id", String.class);
    private final Field<String> STUDENT_ID = DSL.field("student_id", String.class);
    private final Field<byte[]> IMAGE_DATA = DSL.field("image_data", byte[].class);

    public StudentFaceImageRepositoryJooq(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public String create(String studentId, byte[] imageData) {
        String uuid = UUID.randomUUID().toString();
        dsl.insertInto(STUDENT_FACE_IMAGES_TABLE)
            .set(STUDENT_FACE_IMAGE_ID, uuid)
            .set(STUDENT_ID, studentId)
            .set(IMAGE_DATA, imageData)
            .execute();
        return uuid;
    }

    @Override
    public List<StudentFaceImage> fetchFaceImagesByStudentId(String studentId) {
        return dsl.select(STUDENT_FACE_IMAGE_ID, STUDENT_ID, IMAGE_DATA)
            .from(STUDENT_FACE_IMAGES_TABLE)
            .where(STUDENT_ID.eq(studentId))
            .fetch(record -> new StudentFaceImage(
                record.get(STUDENT_FACE_IMAGE_ID),
                record.get(STUDENT_ID),
                record.get(IMAGE_DATA)
            ));
    }

    @Override
    public boolean deleteFaceImagesByStudentId(String studentId) {
        int rowsAffected = dsl.deleteFrom(STUDENT_FACE_IMAGES_TABLE)
            .where(STUDENT_ID.eq(studentId))
            .execute();
        return rowsAffected > 0;
    }
}
