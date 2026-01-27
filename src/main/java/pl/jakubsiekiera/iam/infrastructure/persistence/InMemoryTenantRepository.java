package pl.jakubsiekiera.iam.infrastructure.persistence;

import pl.jakubsiekiera.iam.domain.model.tenant.Tenant;
import pl.jakubsiekiera.iam.domain.model.tenant.TenantId;
import pl.jakubsiekiera.iam.domain.repository.TenantRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTenantRepository implements TenantRepository {
    
    // Simulating a database table
    private final Map<TenantId, Tenant> store = new ConcurrentHashMap<>();

    @Override
    public void save(Tenant tenant) {
        store.put(tenant.getId(), tenant);
    }

    @Override
    public Optional<Tenant> findById(TenantId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public boolean existsByName(String name) {
        return store.values().stream()
                .anyMatch(t -> t.getName().equalsIgnoreCase(name));
    }
}