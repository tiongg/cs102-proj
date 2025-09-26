package g1t1.models.interfaces;

import g1t1.models.ids.BaseID;

/**
 * Has ID, name & email property to update
 */
public interface HasDetails<T extends BaseID> extends HasProperty {
    public void setId(T id);

    public void setEmail(String email);

    public void setName(String name);
}
