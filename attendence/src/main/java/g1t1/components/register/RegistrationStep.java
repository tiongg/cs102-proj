package g1t1.components.register;

import g1t1.models.interfaces.HasProperty;
import javafx.beans.property.BooleanProperty;

public interface RegistrationStep<T extends HasProperty> {
    public BooleanProperty validProperty();

    public void setProperty(T value);
}
