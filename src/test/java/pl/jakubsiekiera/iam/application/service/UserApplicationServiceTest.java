package pl.jakubsiekiera.iam.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.jakubsiekiera.iam.application.dto.RegisterUserCommand;
import pl.jakubsiekiera.iam.application.dto.UserResponse;
import pl.jakubsiekiera.iam.domain.model.tenant.Tenant;
import pl.jakubsiekiera.iam.domain.model.tenant.TenantId;
import pl.jakubsiekiera.iam.domain.model.user.Email;
import pl.jakubsiekiera.iam.domain.model.user.Role;
import pl.jakubsiekiera.iam.domain.model.user.User;
import pl.jakubsiekiera.iam.domain.model.user.UserId;
import pl.jakubsiekiera.iam.domain.repository.TenantRepository;
import pl.jakubsiekiera.iam.domain.repository.UserRepository;
import pl.jakubsiekiera.iam.domain.service.IdentityProvider;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserApplicationService}.
 * Focuses on business logic orchestration between the Domain and External Identity Providers (Keycloak).
 */
@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {

    // Mocked dependencies to isolate the Application Service
    @Mock private UserRepository userRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private IdentityProvider identityProvider;

    private UserApplicationService service;

    @BeforeEach
    void setUp() {
        // Manual instantiation ensures we test the actual service logic
        service = new UserApplicationService(userRepository, tenantRepository, identityProvider);
    }

    // --- Register User Tests ---

    @Test
    @DisplayName("Should create user in Keycloak and then save to DB")
    void shouldRegisterUser() {
        // Arrange: Set up input data and mock behaviors
        var command = new RegisterUserCommand("john_doe", "john@example.com", "password123");
        String generatedKeycloakId = "kc-uuid-999";

        // Scenario: User does not exist yet; Keycloak successfully creates an entry
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());
        when(identityProvider.registerUser(command.username(), command.email(), command.password()))
                .thenReturn(generatedKeycloakId);

        // Act: Execute the service method
        UserResponse response = service.registerUser(command);

        // Assert: Verify that the internal database save happened with the correct linked ID
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail().value()).isEqualTo("john@example.com");
        // CRITICAL: Ensure the local user record is linked to the Identity Provider (Keycloak) UUID
        assertThat(savedUser.getKeycloakId()).isEqualTo(generatedKeycloakId); 
        
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception if email is already taken locally")
    void shouldFailIfUserExists() {
        // Arrange
        var command = new RegisterUserCommand("john_doe", "john@example.com", "password123");
        
        // Mock: The database already has a user with this email
        when(userRepository.findByEmail(any(Email.class)))
                .thenReturn(Optional.of(mock(User.class)));

        // Act & Assert: Verify that an exception is thrown before external calls are made
        assertThatThrownBy(() -> service.registerUser(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email already in use");

        // Safety Check: Verify Keycloak was NEVER called to prevent "ghost" users in the IDP
        verify(identityProvider, never()).registerUser(anyString(), anyString(), anyString());
    }

    // --- Invite User Tests ---

    @Test
    @DisplayName("Should add role to user for specific tenant")
    void shouldInviteUserToTenant() {
        // Arrange
        String tenantIdStr = UUID.randomUUID().toString();
        String emailStr = "john@example.com";
        String roleName = "ADMIN";

        TenantId tenantId = TenantId.fromString(tenantIdStr);
        Tenant mockTenant = mock(Tenant.class);
        
        // Use a real User domain object to test state mutation correctly
        User realUser = User.register(UserId.generate(), "kc-123", new Email(emailStr));

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(mockTenant));
        when(userRepository.findByEmail(new Email(emailStr))).thenReturn(Optional.of(realUser));

        // Act
        service.inviteUserToTenant(tenantIdStr, emailStr, roleName);

        // Assert: Ensure the updated user state is persisted
        verify(userRepository).save(realUser);
        
        // Verify the domain model logic: User should now have the 'ADMIN' role for this specific tenant
        assertThat(realUser.getRolesForTenant(tenantId))
                .extracting(Role::name)
                .containsExactly("ADMIN");
    }

    @Test
    @DisplayName("Should throw exception when inviting to non-existent tenant")
    void shouldFailInviteUnknownTenant() {
        // Arrange: Mocking a repository miss (Tenant not found)
        String randomId = UUID.randomUUID().toString();
        when(tenantRepository.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.inviteUserToTenant(randomId, "a@b.com", "ADMIN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant not found");
    }
}