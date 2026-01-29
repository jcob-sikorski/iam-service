package pl.jakubsiekiera.iam.domain.event;

import pl.jakubsiekiera.iam.domain.model.tenant.TenantId;
import java.time.Instant;

// Events are past tense and immutable
public record TenantRegisteredEvent(
    TenantId tenantId, 
    String name, 
    Instant occurredOn
) {}
