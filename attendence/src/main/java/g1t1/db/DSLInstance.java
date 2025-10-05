package g1t1.db;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;

public class DSLInstance implements AutoCloseable {
    public final DSLContext dsl;
    private final Connection connection;

    public DSLInstance() throws SQLException, DataAccessException {
        DatabaseInitializer db = new DatabaseInitializer();
        this.connection = db.connect();
        this.dsl = DSL.using(connection, SQLDialect.SQLITE);
    }

    @Override
    public void close() throws SQLException {
        if (this.connection != null) {
            this.connection.close();
        }
    }
}
