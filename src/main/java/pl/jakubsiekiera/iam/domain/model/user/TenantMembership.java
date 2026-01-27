package pl.jakubsiekiera.iam.domain.model.user;

import pl.jakubsiekiera.iam.domain.model.tenant.TenantId;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

public class TenantMembership {
    private final TenantId tenantId;
    private final Set<Role> roles;

    public TenantMembership(TenantId tenantId, Role initialRole) {
        this.tenantId = tenantId;
        this.roles = new HashSet<>();
        this.roles.add(initialRole);
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }
    
    public TenantId getTenantId() {
        return tenantId;
    }
}