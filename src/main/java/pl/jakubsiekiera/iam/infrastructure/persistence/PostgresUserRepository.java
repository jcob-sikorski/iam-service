package pl.jakubsiekiera.iam.infrastructure.persistence;

// Domain imports: These represent the "What" (Business Logic)
import pl.jakubsiekiera.iam.domain.model.tenant.TenantId;
import pl.jakubsiekiera.iam.domain.model.user.*;
import pl.jakubsiekiera.iam.domain.repository.UserRepository;

// Infrastructure imports: These represent the "How" (Database Technology)
import pl.jakubsiekiera.iam.infrastructure.persistence.entity.UserJpaEntity;
import pl.jakubsiekiera.iam.infrastructure.persistence.entity.UserMembershipJpaEntity;
import pl.jakubsiekiera.iam.infrastructure.persistence.repository.JpaUserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Collectors;

@Repository // Marks this class as a Spring-managed bean for data access
@Primary // Ensures this implementation is chosen if multiple UserRepository beans exist
@RequiredArgsConstructor // Lombok generates a constructor for all final fields (Dependency Injection)
public class PostgresUserRepository implements UserRepository {

    // The Spring Data JPA interface that handles actual SQL execution
    private final JpaUserRepository jpaRepository;

    @Override
    public void save(User user) {
        // 1. Convert the rich Domain object into a flat JPA Entity
        UserJpaEntity entity = toEntity(user);
        // 2. Persist the entity to the database via Hibernate
        jpaRepository.save(entity);
    }

    @Override
    public Optional<User> findById(UserId id) {
        // Fetch from DB using the raw UUID, then map the result (if present) back to a Domain object
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        // Fetch from DB using the raw String email, then map the result back to a Domain object
        return jpaRepository.findByEmail(email.value()).map(this::toDomain);
    }

    // --- MAPPERS: Transforming data between layers ---

    /**
     * Converts a Domain 'User' (Logic-rich) to a 'UserJpaEntity' (Database-friendly)
     */
    private UserJpaEntity toEntity(User domain) {
        UserJpaEntity entity = new UserJpaEntity(); // Initialize the JPA container
        entity.setId(domain.getId().value()); // Extract raw UUID from Value Object
        entity.setKeycloakId(domain.getKeycloakId()); // Map KeycloakId
        entity.setEmail(domain.getEmail().value());
        // Transform the Map of Domain Memberships into a List of JPA Entities
        var membershipEntities = domain.getMemberships().values().stream().map(m -> {
            UserMembershipJpaEntity me = new UserMembershipJpaEntity();
            me.setUser(entity); // Set the circular reference (Foreign Key link) required by JPA
            me.setTenantId(m.getTenantId().value()); // Extract the raw Tenant UUID
            
            // Flatten the List of Role Enums into a single CSV string (e.g., "ADMIN,USER")
            String roleStr = m.getRoles().stream()
                                     .map(Role::name)
                                     .collect(Collectors.joining(","));
            me.setRoles(roleStr); // Store the CSV string in the entity
            return me;
        }).collect(Collectors.toList());

        entity.setMemberships(membershipEntities); // Attach the collection to the parent entity
        return entity;
}

    /**
     * Converts a 'UserJpaEntity' (Database data) back into a Domain 'User' (Business Logic)
     */
private User toDomain(UserJpaEntity entity) {
    User user = new User(
        new UserId(entity.getId()), // Wrap raw UUID back into a Value Object
        entity.getKeycloakId(), // Map KeycloakId
        new Email(entity.getEmail()) // Wrap raw String back into an Email Value Object
    );
        // Iterate through stored memberships to rebuild the domain state
        for (UserMembershipJpaEntity me : entity.getMemberships()) {
            TenantId tid = new TenantId(me.getTenantId()); // Reconstruct the TenantId
            // Split the CSV string back into individual role strings
            for (String r : me.getRoles().split(",")) {
                // If role exists, add it to the domain object's internal state
                if (!r.isBlank()) user.addToTenant(tid, new Role(r));
            }
        }
        return user; // Return the fully "rehydrated" Domain object
    }
}