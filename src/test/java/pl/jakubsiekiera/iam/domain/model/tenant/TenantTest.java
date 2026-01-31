package pl.jakubsiekiera.iam.domain.model.tenant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the {@link Tenant} aggregate root.
 * Validates business rules for tenant registration, lifecycle changes, and invariants.
 */
class TenantTest {

    @Test
    @DisplayName("Should register a new active tenant")
    void shouldRegisterTenant() {
        // Arrange: Prepare the unique identity and required attributes
        TenantId id = TenantId.generate();
        String name = "Acme Corp";

        // Act: Invoke the factory method to create a new tenant instance
        Tenant tenant = Tenant.register(id, name);

        // Assert: Verify the tenant is created with correct state and default values
        assertThat(tenant).isNotNull();
        assertThat(tenant.getId()).isEqualTo(id);
        assertThat(tenant.getName()).isEqualTo(name);
        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
        assertThat(tenant.getCreationDate()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    @DisplayName("Should throw exception when registering with empty name")
    void shouldThrowOnEmptyName() {
        // Arrange
        TenantId id = TenantId.generate();
        
        // Act & Assert: Verify that business invariants (non-empty names) are enforced
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
        // Arrange: Start with a freshly registered (active) tenant
        Tenant tenant = Tenant.register(TenantId.generate(), "Acme Corp");

        // Act: Perform the state transition
        tenant.suspend();

        // Assert: Verify the status has changed to SUSPENDED
        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
    }

    @Test
    @DisplayName("Should activate a suspended tenant")
    void shouldActivateTenant() {
        // Arrange: Prepare a tenant in a SUSPENDED state
        Tenant tenant = Tenant.register(TenantId.generate(), "Acme Corp");
        tenant.suspend(); 

        // Act: Attempt to bring the tenant back to ACTIVE
        tenant.activate();

        // Assert: Verify successful transition
        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    }

    @Test
    @DisplayName("Activate should be idempotent (do nothing if already active)")
    void shouldBeIdempotentOnActivate() {
        // Arrange: Tenant is ACTIVE by default upon registration
        Tenant tenant = Tenant.register(TenantId.generate(), "Acme Corp");
        
        // Act: Activate an already active tenant
        tenant.activate(); 

        // Assert: Status remains unchanged (no side effects or exceptions)
        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    }
}