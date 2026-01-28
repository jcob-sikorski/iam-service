package pl.jakubsiekiera.iam.domain.model.tenant;

import lombok.Getter;
import java.time.Instant;

@Getter
public class Tenant {

    private final TenantId id;
    private String name;
    private TenantStatus status;
    private final Instant creationDate;

    // We will add subscriptionPlan later

    // Private constructor for internal use / hydration
    public Tenant(TenantId id, String name, TenantStatus status, Instant creationDate) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.creationDate = creationDate;
    }

    // Factory method: "RegisterTenant" command results in this
    public static Tenant register(TenantId id, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tenant name cannot be empty");
        }
        // Default state is ACTIVE (or PENDING if you require email verification step first)
        return new Tenant(id, name, TenantStatus.ACTIVE, Instant.now());
    }

    // Business Behavior: Activate
    public void activate() {
        // Invariant check
        if (this.status == TenantStatus.ACTIVE) {
            return; // idempotent
        }
        // Logic: ensure payment info exists? (We'll add that check later)
        this.status = TenantStatus.ACTIVE;
    }

    // Business Behavior: Suspend
    public void suspend() {
        this.status = TenantStatus.SUSPENDED;
    }
}