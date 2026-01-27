package pl.jakubsiekiera.iam.infrastructure.persistence;

import pl.jakubsiekiera.iam.domain.model.user.Email;
import pl.jakubsiekiera.iam.domain.model.user.User;
import pl.jakubsiekiera.iam.domain.model.user.UserId;
import pl.jakubsiekiera.iam.domain.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<UserId, User> store = new ConcurrentHashMap<>();

    @Override
    public void save(User user) {
        store.put(user.getId(), user);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return store.values().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }
}