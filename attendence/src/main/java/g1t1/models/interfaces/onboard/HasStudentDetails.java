package g1t1.models.interfaces.onboard;

import g1t1.models.ids.StudentID;
import g1t1.models.interfaces.register.HasDetails;
import g1t1.models.sessions.ModuleSection;

public interface HasStudentDetails extends HasDetails<StudentID> {
    public void setModuleSection(ModuleSection moduleSection);
}
