package g1t1.models.interfaces.register;

import g1t1.models.ids.BaseID;
import g1t1.models.interfaces.HasProperty;

/**
 * Has ID, name & email property to update
 */
public interface HasDetails<T extends BaseID> extends HasProperty {
    public void setId(T id);

    public void setEmail(String email);

    public void setName(String name);
}
