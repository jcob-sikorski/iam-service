package pl.jakubsiekiera.iam.domain.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.jakubsiekiera.iam.domain.model.tenant.TenantId;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the {@link User} aggregate root.
 * Verifies user registration, tenant membership logic, and role management.
 */
class UserTest {

    // Test constants to ensure consistency across test cases
    private final UserId userId = UserId.generate();
    private final Email email = new Email("john@example.com");
    private final String keycloakId = "kc-123";

    /**
     * Verifies that the factory method correctly initializes a User
     * with the provided identity and email, ensuring no leaking state (memberships).
     */
    @Test
    @DisplayName("Should register a user without initial memberships")
    void shouldRegisterUser() {
        // Act: Use the domain factory method to create a new user
        User user = User.register(userId, keycloakId, email);

        // Assert: Verify state integrity
        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getMemberships()).isEmpty();
    }

    /**
     * Verifies the successful association of a user with a specific tenant
     * and the assignment of an initial role.
     */
    @Test
    @DisplayName("Should add user to a tenant with a role")
    void shouldAddToTenant() {
        // Arrange
        User user = User.register(userId, keycloakId, email);
        TenantId tenantId = TenantId.generate();

        // Act: Add the user to a new tenant
        user.addToTenant(tenantId, Role.ADMIN);

        // Assert: Check that membership exists and roles are correctly assigned
        assertThat(user.getMemberships()).containsKey(tenantId);
        assertThat(user.getRolesForTenant(tenantId)).containsExactly(Role.ADMIN);
    }

    /**
     * Verifies that adding a role to a tenant the user already belongs to
     * results in a collection of unique roles (idempotency/set logic).
     */
    @Test
    @DisplayName("Should add additional role to existing tenant membership")
    void shouldAddRoleToExistingMembership() {
        // Arrange: Prepare a user who already belongs to a tenant
        User user = User.register(userId, keycloakId, email);
        TenantId tenantId = TenantId.generate();
        user.addToTenant(tenantId, Role.MEMBER);

        // Act: Add a second, different role to the same tenant
        user.addToTenant(tenantId, Role.ADMIN);

        // Assert: Ensure both roles are present
        assertThat(user.getRolesForTenant(tenantId))
                .hasSize(2)
                .contains(Role.MEMBER, Role.ADMIN);
    }

    /**
     * Verifies the boundary case where querying roles for a tenant
     * the user has no association with returns an empty collection instead of null.
     */
    @Test
    @DisplayName("Should return empty roles for unknown tenant")
    void shouldReturnEmptyForUnknownTenant() {
        // Arrange
        User user = User.register(userId, keycloakId, email);
        
        // Act & Assert: Querying a random/new tenant ID should be safe
        assertThat(user.getRolesForTenant(TenantId.generate())).isEmpty();
    }
}