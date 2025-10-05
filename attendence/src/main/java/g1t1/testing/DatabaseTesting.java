package g1t1.testing;

import g1t1.db.DatabaseInitializer;

public class DatabaseTesting {
    public static void main(String[] args) {
        DatabaseInitializer db = new DatabaseInitializer();
        db.init();
    }
}
