package pl.jakubsiekiera.iam.domain.model.tenant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.assertj.core.api.Assertions.*;

class TenantTest {

    @Test
    @DisplayName("Should register a new active tenant")
    void shouldRegisterTenant() {
        // Arrange
        TenantId id = TenantId.generate();
        String name = "Acme Corp";

        // Act
        Tenant tenant = Tenant.register(id, name);

        // Assert
        assertThat(tenant).isNotNull();
        assertThat(tenant.getId()).isEqualTo(id);
        assertThat(tenant.getName()).isEqualTo(name);
        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
        assertThat(tenant.getCreationDate()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    @DisplayName("Should throw exception when registering with empty name")
    void shouldThrowOnEmptyName() {
        TenantId id = TenantId.generate();
        
        assertThatThrownBy(() -> Tenant.register(id, ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Tenant name cannot be empty");

        assertThatThrownBy(() -> Tenant.register(id, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Tenant name cannot be empty");
    }

    @Test
    @DisplayName("Should suspend an active tenant")
    void shouldSuspendTenant() {
        // Arrange
        Tenant tenant = Tenant.register(TenantId.generate(), "Acme Corp");

        // Act
        tenant.suspend();

        // Assert
        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
    }

    @Test
    @DisplayName("Should activate a suspended tenant")
    void shouldActivateTenant() {
        // Arrange
        Tenant tenant = Tenant.register(TenantId.generate(), "Acme Corp");
        tenant.suspend(); // Setup as suspended

        // Act
        tenant.activate();

        // Assert
        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    }

    @Test
    @DisplayName("Activate should be idempotent (do nothing if already active)")
    void shouldBeIdempotentOnActivate() {
        // Arrange
        Tenant tenant = Tenant.register(TenantId.generate(), "Acme Corp");
        
        // Act
        tenant.activate(); 

        // Assert
        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    }
}