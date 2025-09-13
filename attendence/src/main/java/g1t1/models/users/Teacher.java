package g1t1.models.users;

import g1t1.models.BaseEntity;
import g1t1.models.ids.TeacherID;

public class Teacher extends BaseEntity {
    private TeacherID ID;
    private String name;
    private String email;
    private String phoneNumber;

    public Teacher(String id) {
        this.ID = new TeacherID(id);
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public String toString() {
        return "Teacher [ID=" + ID + ", name=" + name + ", email=" + email + ", phoneNumber=" + phoneNumber + "]";
    }
}
