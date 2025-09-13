package g1t1.models.ids;

/**
 * Base ID class.
 * 
 * Internally just a string containing the ID.
 * 
 * Prevents us from confusing IDs with each other
 */
public abstract class BaseID {
    protected String id;

    public BaseID(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
