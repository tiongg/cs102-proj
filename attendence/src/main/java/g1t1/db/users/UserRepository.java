package g1t1.db.users;

import java.util.Optional;
import java.util.List;

public interface UserRepository {
  String create(String fullName, String email, String passwordHash);
  List<User> fetchAllUsers();
  Optional<User> findById(String userId);
  Optional<User> findByEmail(String email);
  boolean update(String userId, String fullNameNullable, String emailNullable);
  boolean updatePassword(String userId, String newPasswordHash);
  boolean delete(String userId);
}

