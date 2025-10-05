package g1t1.db.users;

public record User(
    String userId,
    String fullName,
    String email,
    String passwordHash
) {}
