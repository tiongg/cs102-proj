package g1t1.models.users;

import java.util.List;

import g1t1.db.student_face_images.StudentFaceImage;
import g1t1.db.students.StudentRecord;
import g1t1.models.BaseEntity;
import g1t1.models.ids.StudentID;
import g1t1.models.sessions.ModuleSection;
import g1t1.opencv.models.Recognisable;

public class Student extends BaseEntity implements Recognisable {
    private final StudentID id;
    private final String name;
    private final ModuleSection moduleSection;
    private final String email;
    private FaceData faceData;
    private boolean isActive;

    public Student(StudentID id, String name, ModuleSection moduleSection, String email) {
        this.id = id;
        this.name = name;
        this.moduleSection = moduleSection;
        this.email = email;
        this.isActive = true;
    }

    public Student(StudentRecord dbStudent, List<StudentFaceImage> dbFaceImages, ModuleSection moduleSection) {
        this.id = new StudentID(dbStudent.studentId());
        this.name = dbStudent.fullName();
        this.moduleSection = moduleSection;
        this.email = dbStudent.email();
        this.isActive = dbStudent.isActive();
        this.faceData = new FaceData(dbFaceImages.stream().map(StudentFaceImage::imageData).toList());
    }

    public StudentID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getRecognitionId() {
        return this.getId().toString();
    }

    public ModuleSection getModuleSection() {
        return moduleSection;
    }

    public String getEmail() {
        return email;
    }

    public FaceData getFaceData() {
        return this.faceData;
    }

    public void setFaceData(FaceData faceData) {
        this.faceData = faceData;
    }

    public boolean getIsActive() {
        return this.isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "Student [id=" + id + ", name=" + name + ", moduleSection=" + moduleSection + ", email=" + email + "]";
    }
}
