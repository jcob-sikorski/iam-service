package pl.jakubsiekiera.iam.domain.model;

import org.junit.jupiter.api.Test;
import pl.jakubsiekiera.iam.domain.model.tenant.TenantId;
import pl.jakubsiekiera.iam.domain.model.user.Role;
import pl.jakubsiekiera.iam.domain.model.user.UserId;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests for shared Value Objects to ensure domain integrity across the IAM module.
 * These tests verify that essential identifiers and types cannot be created in an invalid state.
 */
class SharedValueObjectsTest {

    @Test
    void tenantIdShouldNotBeNull() {
        // Ensure a TenantId cannot be instantiated without a value, 
        // as every resource must belong to a specific tenant.
        assertThatThrownBy(() -> new TenantId(null))
            .isInstanceOf(IllegalArgumentException.class);
            
        // Verify successful creation from a valid UUID string.
        assertThat(TenantId.fromString(UUID.randomUUID().toString())).isNotNull();
    }

    @Test
    void userIdShouldNotBeNull() {
        // A User cannot exist without a unique identifier; 
        // null checks prevent downstream NullPointerExceptions in the persistence layer.
        assertThatThrownBy(() -> new UserId(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void roleShouldRequireName() {
        // Roles define permissions; a null role would break the security context.
        assertThatThrownBy(() -> new Role(null))
            .isInstanceOf(IllegalArgumentException.class);
            
        // Verify that the domain constants (like ADMIN) are correctly mapped.
        assertThat(Role.ADMIN.name()).isEqualTo("ADMIN");
    }
}