package g1t1.db.users;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserRepositoryJooq implements UserRepository {
    private static final Field<String> USER_ID = DSL.field("user_id", String.class);
    private static final Field<String> FULL_NAME = DSL.field("full_name", String.class);
    private static final Field<String> EMAIL = DSL.field("email", String.class);
    private static final Field<String> PASSWORD_HASH = DSL.field("password_hash", String.class);
    private final DSLContext dsl;
    private final Table<?> USERS_TABLE = DSL.table("users");

    public UserRepositoryJooq(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public String create(String userId, String fullName, String email, String passwordHash) {
        dsl.insertInto(USERS_TABLE)
                .set(USER_ID, userId)
                .set(FULL_NAME, fullName)
                .set(EMAIL, email)
                .set(PASSWORD_HASH, passwordHash)
                .execute();
        return userId;
    }

    @Override
    public List<User> fetchAllUsers() {
        return dsl.select(USER_ID, FULL_NAME, EMAIL, PASSWORD_HASH)
                .from(USERS_TABLE)
                .fetch(record -> new User(
                        record.get(USER_ID),
                        record.get(FULL_NAME),
                        record.get(EMAIL),
                        record.get(PASSWORD_HASH)
                ));
    }

    @Override
    public Optional<User> fetchUserById(String userId) {
        return dsl.select(USER_ID, FULL_NAME, EMAIL, PASSWORD_HASH)
                .from(USERS_TABLE)
                .where(USER_ID.eq(userId))
                .fetchOptional(record -> new User(
                        record.get(USER_ID),
                        record.get(FULL_NAME),
                        record.get(EMAIL),
                        record.get(PASSWORD_HASH)
                ));
    }

    @Override
    public Optional<User> fetchUserByEmail(String email) {
        return dsl.select(USER_ID, FULL_NAME, EMAIL, PASSWORD_HASH)
                .from(USERS_TABLE)
                .where(EMAIL.eq(email))
                .fetchOptional(record -> new User(
                        record.get(USER_ID),
                        record.get(FULL_NAME),
                        record.get(EMAIL),
                        record.get(PASSWORD_HASH)
                ));
    }

    @Override
    public boolean update(String userId, String fullNameNullable, String emailNullable) {
        Map<Field<?>, Object> changes = new HashMap<>();
        if (fullNameNullable != null) changes.put(FULL_NAME, fullNameNullable);
        if (emailNullable != null) changes.put(EMAIL, emailNullable);
        if (changes.isEmpty()) return false;

        return dsl.update(USERS_TABLE)
                .set(changes)
                .where(USER_ID.eq(userId))
                .execute() == 1;
    }

    @Override
    public boolean updatePassword(String userId, String newPasswordHash) {
        return dsl.update(USERS_TABLE)
                .set(PASSWORD_HASH, newPasswordHash)
                .where(USER_ID.eq(userId))
                .execute() == 1;
    }

    @Override
    public boolean delete(String userId) {
        return dsl.delete(USERS_TABLE)
                .where(USER_ID.eq(userId))
                .execute() == 1;
    }
}
