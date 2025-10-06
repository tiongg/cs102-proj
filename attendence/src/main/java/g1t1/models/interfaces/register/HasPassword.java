package g1t1.models.interfaces.register;

import g1t1.models.interfaces.HasProperty;

/**
 * Has password property to update
 */
public interface HasPassword extends HasProperty {
    public void setPassword(String password);
}
