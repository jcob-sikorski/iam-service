package pl.jakubsiekiera.iam.infrastructure.persistence.repository;

import pl.jakubsiekiera.iam.infrastructure.persistence.entity.TenantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

/**
 * Repository interface for Tenant data access.
 * Extends JpaRepository to inherit standard database operations for the Tenant entity.
 * The use of UUID as the ID type ensures globally unique identifiers for multi-tenant environments.
 */
public interface JpaTenantRepository extends JpaRepository<TenantJpaEntity, UUID> {

    /**
     * A Derived Query Method using the 'exists' projection.
     * Spring generates a high-performance "SELECT 1" or "EXISTS" query rather than 
     * loading the entire entity into memory.
     * * @param name The name of the tenant to check.
     * @return true if a record with the given name exists, false otherwise.
     */
    boolean existsByName(String name);
}