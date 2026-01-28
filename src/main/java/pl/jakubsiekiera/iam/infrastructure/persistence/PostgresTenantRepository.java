package pl.jakubsiekiera.iam.infrastructure.persistence;

// Domain imports: Core business logic and identifiers
import pl.jakubsiekiera.iam.domain.model.tenant.Tenant;
import pl.jakubsiekiera.iam.domain.model.tenant.TenantId;
import pl.jakubsiekiera.iam.domain.model.tenant.TenantStatus;
import pl.jakubsiekiera.iam.domain.repository.TenantRepository;

// Infrastructure imports: Database-specific entities and Spring Data interfaces
import pl.jakubsiekiera.iam.infrastructure.persistence.entity.TenantJpaEntity;
import pl.jakubsiekiera.iam.infrastructure.persistence.repository.JpaTenantRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Registers this class as a Spring Bean within the persistence layer
@Primary // Tells Spring: "If multiple TenantRepository beans exist, prioritize this Postgres one"
@RequiredArgsConstructor // Automatically injects the JpaTenantRepository via constructor
public class PostgresTenantRepository implements TenantRepository {

    // The low-level Spring Data JPA interface (the "Port" to the database)
    private final JpaTenantRepository jpaRepository;

    @Override
    public void save(Tenant tenant) {
        // 1. Convert the high-level Domain object into a JPA Entity
        TenantJpaEntity entity = toEntity(tenant);
        // 2. Persist the record to the Postgres database
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Tenant> findById(TenantId id) {
        // Find by raw UUID, then map the found entity back into a Domain object
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        // Directly delegates the check to the JPA repository (optimized exists query)
        return jpaRepository.existsByName(name);
    }

    // --- MAPPERS: The translation logic between layers ---

    /**
     * Translates a Tenant Domain object into a TenantJpaEntity for database storage.
     */
    private TenantJpaEntity toEntity(Tenant domain) {
        TenantJpaEntity entity = new TenantJpaEntity();
        entity.setId(domain.getId().value()); // Extracts the raw UUID from the TenantId Value Object
        entity.setName(domain.getName()); // Maps the plain string name
        entity.setStatus(domain.getStatus().name()); // Converts Enum to String (e.g., "ACTIVE")
        entity.setCreationDate(domain.getCreationDate()); // Passes the timestamp
        return entity;
    }

    /**
     * Translates a TenantJpaEntity (database record) back into a rich Domain object.
     * This process is often called "Rehydration."
     */
    private Tenant toDomain(TenantJpaEntity entity) {
        // Reconstructs the Domain object using its internal state recovered from the DB
        return new Tenant(
            new TenantId(entity.getId()), // Wraps the raw UUID back into a Domain Value Object
            entity.getName(), // Passes the raw string name
            TenantStatus.valueOf(entity.getStatus()), // Converts the String status back into a typed Enum
            entity.getCreationDate() // Passes the database timestamp
        );
    }
}