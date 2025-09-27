package g1t1.models.ids;

public class TeacherID extends BaseID {
    public TeacherID(String id) {
        super(id);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TeacherID) {
            return other.toString().equals(this.toString());
        }
        return false;
    }
}
