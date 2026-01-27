package pl.jakubsiekiera.iam.domain.model.tenant;

import java.util.UUID;

public record TenantId(UUID value) {
    public TenantId {
        if (value == null) {
            throw new IllegalArgumentException("TenantId value cannot be null");
        }
    }

    // Factory method for generating new IDs
    public static TenantId generate() {
        return new TenantId(UUID.randomUUID());
    }

    // Factory method for string parsing (useful for APIs)
    public static TenantId fromString(String uuid) {
        return new TenantId(UUID.fromString(uuid));
    }
}