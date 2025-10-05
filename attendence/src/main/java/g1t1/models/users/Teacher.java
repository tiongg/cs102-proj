package g1t1.models.users;

import g1t1.models.BaseEntity;
import g1t1.models.ids.TeacherID;

public class Teacher extends BaseEntity {
    private final TeacherID ID;
    private final String name;
    private final String email;
    private final FaceData faceData;

    public Teacher(String id, String name, String email, FaceData faceData) {
        this.ID = new TeacherID(id);
        this.name = name;
        this.email = email;
        this.faceData = faceData;
    }

    public TeacherID getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "Teacher [ID=" + ID + ", name=" + name + ", email=" + email + "]";
    }
}
