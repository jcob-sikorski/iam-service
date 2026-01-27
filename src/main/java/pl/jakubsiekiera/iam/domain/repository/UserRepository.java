package pl.jakubsiekiera.iam.domain.repository;

import pl.jakubsiekiera.iam.domain.model.user.Email;
import pl.jakubsiekiera.iam.domain.model.user.User;
import pl.jakubsiekiera.iam.domain.model.user.UserId;
import java.util.Optional;

public interface UserRepository {
    void save(User user);
    Optional<User> findById(UserId id);
    Optional<User> findByEmail(Email email);
}