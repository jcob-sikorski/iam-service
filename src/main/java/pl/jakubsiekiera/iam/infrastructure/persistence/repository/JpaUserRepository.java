package pl.jakubsiekiera.iam.infrastructure.persistence.repository;

import pl.jakubsiekiera.iam.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

/**
 * JpaRepository provides standard CRUD operations (save, delete, findById, etc.).
 * UserJpaEntity: The database table being mapped.
 * UUID: The data type of the Primary Key (@Id) in the entity.
 */
public interface JpaUserRepository extends JpaRepository<UserJpaEntity, UUID> {

    /**
     * This is a "Derived Query Method."
     * Spring Data JPA parses the method name 'findByEmail' and automatically
     * generates the SQL: "SELECT * FROM users WHERE email = ?"
     * * Using Optional<> prevents NullPointerExceptions by forcing the caller
     * to handle cases where no user exists with the given email.
     */
    Optional<UserJpaEntity> findByEmail(String email);
}