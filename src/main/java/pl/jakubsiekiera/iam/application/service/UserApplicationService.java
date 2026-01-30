package pl.jakubsiekiera.iam.application.service;

import pl.jakubsiekiera.iam.application.dto.RegisterUserCommand;
import pl.jakubsiekiera.iam.application.dto.UserResponse;
import pl.jakubsiekiera.iam.domain.model.user.Email;
import pl.jakubsiekiera.iam.domain.model.user.User;
import pl.jakubsiekiera.iam.domain.model.user.UserId;
import pl.jakubsiekiera.iam.domain.model.tenant.TenantId;
import pl.jakubsiekiera.iam.domain.model.user.Role;
import pl.jakubsiekiera.iam.domain.repository.UserRepository;
import pl.jakubsiekiera.iam.domain.repository.TenantRepository;
import pl.jakubsiekiera.iam.domain.service.IdentityProvider; // Import the new interface
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final IdentityProvider identityProvider; // Inject the IDP adapter

    @Transactional
    public UserResponse registerUser(RegisterUserCommand command) {
        Email email = new Email(command.email());

        // 1. Check local uniqueness
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already in use: " + command.email());
        }

        // 2. CALL KEYCLOAK (The Missing Step)
        // We delegate password handling to Keycloak. It returns the unique 'sub' ID.
        String keycloakId = identityProvider.registerUser(
            command.username(), 
            command.email(), 
            command.password()
        );

        // 3. Create Aggregate using the ID from Keycloak
        User user = User.register(
            UserId.generate(),
            keycloakId, // Now this is not null!
            email
        );

        userRepository.save(user);

        return UserResponse.from(user);
    }
    
    @Transactional
    public void inviteUserToTenant(String tenantIdStr, String emailStr, String roleName) {
        TenantId tenantId = TenantId.fromString(tenantIdStr);
        if (tenantRepository.findById(tenantId).isEmpty()) {
            throw new IllegalArgumentException("Tenant not found: " + tenantIdStr);
        }

        Email email = new Email(emailStr);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + emailStr));

        Role role = new Role(roleName);
        user.addToTenant(tenantId, role);
        userRepository.save(user);
    }
}