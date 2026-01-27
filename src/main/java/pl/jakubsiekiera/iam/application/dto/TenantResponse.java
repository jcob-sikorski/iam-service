package pl.jakubsiekiera.iam.application.dto;

import pl.jakubsiekiera.iam.domain.model.tenant.Tenant;
import java.time.Instant;
import java.util.UUID;

public record TenantResponse(
    UUID id,
    String name,
    String status,
    Instant creationDate
) {
    // Static factory method to convert Domain Entity -> DTO
    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
            tenant.getId().value(), // Unwrap the TenantId VO
            tenant.getName(),
            tenant.getStatus().name(), // Convert Enum to String
            tenant.getCreationDate()
        );
    }
}