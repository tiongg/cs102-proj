package g1t1.db.user_face_images;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.UUID;

public class UserFaceImageRepositoryJooq implements UserFaceImageRepository {
    private final DSLContext dsl;

    private final Table<?> USER_FACE_IMAGES_TABLE = DSL.table("user_face_images");
    private final Field<String> USER_FACE_IMAGE_ID = DSL.field("face_image_id", String.class);
    private final Field<String> USER_ID = DSL.field("user_id", String.class);
    private final Field<byte[]> IMAGE_DATA = DSL.field("face_image", byte[].class);

    public UserFaceImageRepositoryJooq(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public String create(String userId, byte[] imageData) {
        String uuid = UUID.randomUUID().toString();
        dsl.insertInto(USER_FACE_IMAGES_TABLE)
                .set(USER_FACE_IMAGE_ID, uuid)
                .set(USER_ID, userId)
                .set(IMAGE_DATA, imageData)
                .execute();
        return uuid;
    }

    @Override
    public List<UserFaceImage> fetchFaceImagesByUserId(String userId) {
        return dsl.select(USER_FACE_IMAGE_ID, USER_ID, IMAGE_DATA)
                .from(USER_FACE_IMAGES_TABLE)
                .where(USER_ID.eq(userId))
                .fetch(record -> new UserFaceImage(
                        record.get(USER_FACE_IMAGE_ID),
                        record.get(USER_ID),
                        record.get(IMAGE_DATA)
                ));
    }

    @Override
    public boolean deleteFaceImagesByUserId(String userId) {
        int rowsAffected = dsl.deleteFrom(USER_FACE_IMAGES_TABLE)
                .where(USER_ID.eq(userId))
                .execute();
        return rowsAffected > 0;
    }

}
