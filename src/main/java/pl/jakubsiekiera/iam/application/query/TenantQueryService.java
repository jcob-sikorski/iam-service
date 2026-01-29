package pl.jakubsiekiera.iam.application.query;

import pl.jakubsiekiera.iam.application.dto.Member;
import pl.jakubsiekiera.iam.application.dto.TenantDetails;
import pl.jakubsiekiera.iam.infrastructure.persistence.entity.TenantJpaEntity;
import pl.jakubsiekiera.iam.infrastructure.persistence.entity.UserJpaEntity;
import pl.jakubsiekiera.iam.infrastructure.persistence.repository.JpaTenantRepository;
import pl.jakubsiekiera.iam.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service responsible for read-only operations regarding Tenant information.
 * This service bypasses the Domain Model to provide a high-performance "Query" layer,
 * directly mapping JPA entities to Data Transfer Objects (DTOs).
 */
@Service
@RequiredArgsConstructor
/* * Transactional readOnly = true tells Spring/Hibernate to skip dirty checking 
 * and optimization for flush modes, reducing memory overhead for read operations.
 */
@Transactional(readOnly = true) 
public class TenantQueryService {

    private final JpaTenantRepository tenantRepo;
    private final JpaUserRepository userRepo;

    /**
     * Retrieves comprehensive details about a specific tenant, including its active members.
     * * @param tenantId The unique identifier of the tenant.
     * @return TenantDetails containing metadata and a list of members with their roles.
     * @throws IllegalArgumentException if the tenant does not exist.
     */
    public TenantDetails getTenantDetails(UUID tenantId) {
        
        // 1. Fetch Tenant basic info
        // We use the JPA Entity directly here because we don't need complex domain logic
        // or invariant enforcement required for write operations.
        TenantJpaEntity tenant = tenantRepo.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        // 2. Fetch associated Members
        // Uses a custom repository query to avoid loading all users and filtering in memory.
        List<UserJpaEntity> users = userRepo.findUsersByTenantId(tenantId);

        // 3. Map Entities to DTOs
        // This transformation layer ensures the API doesn't leak internal database structures.
        List<Member> memberDtos = users.stream()
                .map(u -> {
                    /*
                     * A user can belong to multiple tenants. We filter the membership 
                     * collection to find the specific role the user holds within THIS tenant.
                     */
                    String roles = u.getMemberships().stream()
                            .filter(m -> m.getTenantId().equals(tenantId))
                            .findFirst()
                            .map(m -> m.getRoles())
                            .orElse(""); // Default to empty string if no specific roles found
                    
                    return new Member(u.getEmail(), roles);
                })
                .toList();

        // Construct and return the final read-model DTO
        return new TenantDetails(
                tenant.getId(),
                tenant.getName(),
                tenant.getStatus(),
                tenant.getCreationDate(),
                memberDtos
        );
    }
}