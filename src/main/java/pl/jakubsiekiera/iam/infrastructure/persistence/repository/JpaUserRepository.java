package pl.jakubsiekiera.iam.infrastructure.persistence.repository;

import pl.jakubsiekiera.iam.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    /**
     * CUSTOM JPQL QUERY: Efficient Tenant-Based Lookup
     * * Instead of loading a Tenant aggregate and then accessing its user collection 
     * (which could trigger LazyLoading or fetch unnecessary data), this query 
     * performs a direct JOIN at the database level. 
     * * It filters users based on the 'tenantId' field within the Membership table, 
     * ensuring we only retrieve the specific User entities required for this 
     * context without overhead.
     */
    @Query("""
        SELECT u FROM UserJpaEntity u 
        JOIN u.memberships m 
        WHERE m.tenantId = :tenantId
    """)
    List<UserJpaEntity> findUsersByTenantId(@Param("tenantId") UUID tenantId);
}