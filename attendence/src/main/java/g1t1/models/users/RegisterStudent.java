package g1t1.models.users;

import g1t1.models.ids.StudentID;
import g1t1.models.interfaces.onboard.HasStudentDetails;
import g1t1.models.interfaces.register.HasFaces;
import g1t1.models.sessions.ModuleSection;

/**
 * Student created from onboarding step
 */
public class RegisterStudent implements HasStudentDetails, HasFaces {
    private ModuleSection moduleSection;
    private StudentID studentID;
    private String email;
    private String name;
    private FaceData faceData;
    private byte[] thumbnail;

    @Override
    public void setId(StudentID id) {
        this.studentID = id;
    }

    public ModuleSection getModuleSection() {
        return moduleSection;
    }

    @Override
    public void setModuleSection(ModuleSection moduleSection) {
        this.moduleSection = moduleSection;
    }

    public StudentID getStudentID() {
        return studentID;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public FaceData getFaceData() {
        return faceData;
    }

    @Override
    public void setFaceData(FaceData faceData) {
        this.faceData = faceData;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    @Override
    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }
}
