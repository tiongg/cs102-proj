package g1t1.models.ids;

public class StudentID extends BaseID {
    public StudentID(String id) {
        super(id);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof StudentID) {
            return other.toString().equals(this.toString());
        }
        return false;
    }
}
