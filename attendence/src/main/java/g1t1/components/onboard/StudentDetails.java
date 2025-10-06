package g1t1.components.onboard;

import g1t1.components.register.RegistrationStep;
import g1t1.models.ids.StudentID;
import g1t1.models.interfaces.onboard.HasStudentDetails;
import javafx.beans.property.BooleanProperty;

public class StudentDetails implements RegistrationStep<HasStudentDetails> {
    @Override
    public BooleanProperty validProperty() {
        return null;
    }

    @Override
    public void setProperty(HasStudentDetails target) {
        target.setId(new StudentID("123"));
        target.setName("");
        target.setEmail("");
        target.setModuleSection(null);
    }
}
