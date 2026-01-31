package pl.jakubsiekiera.iam.domain.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.jakubsiekiera.iam.domain.model.tenant.TenantId;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    private final UserId userId = UserId.generate();
    private final Email email = new Email("john@example.com");
    private final String keycloakId = "kc-123";

    @Test
    @DisplayName("Should register a user without initial memberships")
    void shouldRegisterUser() {
        User user = User.register(userId, keycloakId, email);

        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getMemberships()).isEmpty();
    }

    @Test
    @DisplayName("Should add user to a tenant with a role")
    void shouldAddToTenant() {
        // Arrange
        User user = User.register(userId, keycloakId, email);
        TenantId tenantId = TenantId.generate();

        // Act
        user.addToTenant(tenantId, Role.ADMIN);

        // Assert
        assertThat(user.getMemberships()).containsKey(tenantId);
        assertThat(user.getRolesForTenant(tenantId)).containsExactly(Role.ADMIN);
    }

    @Test
    @DisplayName("Should add additional role to existing tenant membership")
    void shouldAddRoleToExistingMembership() {
        // Arrange
        User user = User.register(userId, keycloakId, email);
        TenantId tenantId = TenantId.generate();
        user.addToTenant(tenantId, Role.MEMBER);

        // Act
        user.addToTenant(tenantId, Role.ADMIN);

        // Assert
        assertThat(user.getRolesForTenant(tenantId))
                .hasSize(2)
                .contains(Role.MEMBER, Role.ADMIN);
    }

    @Test
    @DisplayName("Should return empty roles for unknown tenant")
    void shouldReturnEmptyForUnknownTenant() {
        User user = User.register(userId, keycloakId, email);
        assertThat(user.getRolesForTenant(TenantId.generate())).isEmpty();
    }
}