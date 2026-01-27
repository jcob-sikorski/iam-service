package pl.jakubsiekiera.iam.domain.repository;

import pl.jakubsiekiera.iam.domain.model.tenant.Tenant;
import pl.jakubsiekiera.iam.domain.model.tenant.TenantId;
import java.util.Optional;

public interface TenantRepository {
    void save(Tenant tenant);
    Optional<Tenant> findById(TenantId id);
    boolean existsByName(String name);
}