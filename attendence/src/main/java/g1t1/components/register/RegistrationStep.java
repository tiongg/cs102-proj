package g1t1.components.register;

import g1t1.models.interfaces.HasProperty;
import javafx.beans.property.BooleanProperty;

public interface RegistrationStep<T extends HasProperty> {
    public BooleanProperty validProperty();

    /*
    Set property of target registrant
     */
    public default void setProperty(T value) {
    }

    /*
    When tab is stepped to
     */
    public default void onMount(Object currentRegistrant) {
    }

    /*
    When tab is stepped off
     */
    public default void onUnmount() {
    }
}
