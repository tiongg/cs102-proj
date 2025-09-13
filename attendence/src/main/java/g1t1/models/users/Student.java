package g1t1.models.users;

import g1t1.models.BaseEntity;
import g1t1.models.ids.StudentID;
import g1t1.models.sessions.ModuleSection;

public class Student extends BaseEntity {
    private StudentID id;
    private String name;
    private ModuleSection moduleSection;
    private String email;
    private String phoneNumber;
    private FaceData faceData;

    public Student(StudentID id, String name, ModuleSection moduleSection, String email, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.moduleSection = moduleSection;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // #region String constructors
    public Student(String id, String name, String module, String section, String email, String phoneNumber) {
        this(new StudentID(id), name, new ModuleSection(module, section), email, phoneNumber);
    }

    public Student(String id, String name, ModuleSection moduleSection, String email, String phoneNumber) {
        this(new StudentID(id), name, moduleSection, email, phoneNumber);
    }

    public Student(StudentID id, String name, String module, String section, String email, String phoneNumber) {
        this(id, name, new ModuleSection(module, section), email, phoneNumber);
    }
    // #endregion

    public StudentID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ModuleSection getModuleSection() {
        return moduleSection;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public FaceData getFaceData() {
        return this.faceData;
    }

    public void setFaceData(FaceData faceData) {
        this.faceData = faceData;
    }

    @Override
    public String toString() {
        return "Student [id=" + id + ", name=" + name + ", moduleSection=" + moduleSection + ", email=" + email
                + ", phoneNumber=" + phoneNumber + "]";
    }
}
