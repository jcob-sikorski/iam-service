package pl.jakubsiekiera.iam.domain.model.user;

import pl.jakubsiekiera.iam.domain.model.tenant.TenantId;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
public class User {
    private final UserId id;
    private final String keycloakId; // Link to Identity Provider
    private final Email email;
    // NO PASSWORD FIELD HERE
    
    private final Map<TenantId, TenantMembership> memberships = new HashMap<>();

    // Internal Constructor
    public User(UserId id, String keycloakId, Email email) {
        this.id = id;
        this.keycloakId = keycloakId;
        this.email = email;
    }

    // Factory: Register a new user (initially not part of any tenant, or maybe a default one)
    public static User register(UserId id, String keycloakId, Email email) {
        return new User(id, keycloakId, email);
    }

    // Business Logic: Invite/Add to Tenant
    public void addToTenant(TenantId tenantId, Role role) {
        if (memberships.containsKey(tenantId)) {
            // Already a member? Just add the role
            memberships.get(tenantId).addRole(role);
        } else {
            // New membership
            memberships.put(tenantId, new TenantMembership(tenantId, role));
        }
    }

    public Set<Role> getRolesForTenant(TenantId tenantId) {
        if (!memberships.containsKey(tenantId)) {
            return Set.of();
        }
        return memberships.get(tenantId).getRoles();
    }
}