package pl.jakubsiekiera.iam.application.service;

import pl.jakubsiekiera.iam.application.dto.RegisterTenantCommand;
import pl.jakubsiekiera.iam.application.dto.TenantResponse; // Import the new DTO
import pl.jakubsiekiera.iam.domain.model.tenant.Tenant;
import pl.jakubsiekiera.iam.domain.model.tenant.TenantId;
import pl.jakubsiekiera.iam.domain.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantApplicationService {

    private final TenantRepository tenantRepository;

    @Transactional
    public TenantResponse registerTenant(RegisterTenantCommand command) {
        // 1. Validate uniqueness
        if (tenantRepository.existsByName(command.name())) {
            throw new IllegalArgumentException("Tenant with name '" + command.name() + "' already exists");
        }

        // 2. Generate ID and create Aggregate
        TenantId newId = TenantId.generate();
        Tenant newTenant = Tenant.register(newId, command.name());

        // 3. Persist
        tenantRepository.save(newTenant);

        // 4. Return DTO (using the static mapper method)
        return TenantResponse.from(newTenant);
    }
}