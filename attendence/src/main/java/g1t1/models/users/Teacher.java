package g1t1.models.users;

import g1t1.db.user_face_images.UserFaceImage;
import g1t1.db.users.User;
import g1t1.models.BaseEntity;
import g1t1.models.ids.TeacherID;
import g1t1.models.sessions.ModuleSection;
import g1t1.opencv.models.Recognisable;

import java.util.ArrayList;
import java.util.List;

public class Teacher extends BaseEntity implements Recognisable {
    private final TeacherID ID;
    private final String name;
    private final String email;
    private final FaceData faceData;

    private final List<ModuleSection> moduleSections = new ArrayList<>();

    public Teacher(String id, String name, String email, FaceData faceData) {
        this.ID = new TeacherID(id);
        this.name = name;
        this.email = email;
        this.faceData = faceData;
    }

    // From db instances
    public Teacher(User dbUser, List<UserFaceImage> dbFace) {
        this.ID = new TeacherID(dbUser.userId());
        this.name = dbUser.fullName();
        this.email = dbUser.email();
        this.faceData = new FaceData(dbFace.stream().map(UserFaceImage::imageData).toList());
    }

    public TeacherID getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getRecognitionId() {
        return this.getID().toString();
    }

    public String getEmail() {
        return email;
    }

    public FaceData getFaceData() {
        return this.faceData;
    }

    public List<ModuleSection> getModuleSections() {
        return this.moduleSections;
    }

    @Override
    public String toString() {
        return "Teacher [ID=" + ID + ", name=" + name + ", email=" + email + "]";
    }
}
