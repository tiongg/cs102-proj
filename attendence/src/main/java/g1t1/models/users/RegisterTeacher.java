package g1t1.models.users;

import g1t1.models.ids.TeacherID;
import g1t1.models.interfaces.HasFaces;
import g1t1.models.interfaces.HasDetails;
import g1t1.models.interfaces.HasPassword;

import java.util.List;

/**
 * User/Teacher created from the registration step
 */
public class RegisterTeacher implements HasDetails<TeacherID>, HasFaces, HasPassword {
    private TeacherID teacherID;
    private String fullName;
    private String email;
    private String password;

    private List<FaceData> faceData;
    private FaceData thumbnailFace;

    @Override
    public void setThumbnail(FaceData thumbnail) {
        this.thumbnailFace = thumbnail;
    }

    @Override
    public void setId(TeacherID id) {
        this.teacherID = id;
    }

    @Override
    public void setName(String name) {
        this.fullName = name;
    }

    @Override
    public String toString() {
        return "RegisterTeacher{" +
                "password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", teacherID=" + teacherID +
                '}';
    }

    public TeacherID getTeacherID() {
        return teacherID;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    public List<FaceData> getFaceData() {
        return faceData;
    }

    @Override
    public void setFaceData(List<FaceData> faceData) {
        this.faceData = faceData;
    }

    public FaceData getThumbnailFace() {
        return thumbnailFace;
    }
}
