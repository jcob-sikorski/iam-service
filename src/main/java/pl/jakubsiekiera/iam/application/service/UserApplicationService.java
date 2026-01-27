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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder; // Spring Security's interface

    @Transactional
    public UserResponse registerUser(RegisterUserCommand command) {
        Email email = new Email(command.email());

        // 1. Check uniqueness (simple check for now)
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already in use: " + command.email());
        }

        // 2. Hash password
        String hashedPassword = passwordEncoder.encode(command.password());

        // 3. Create Aggregate
        User user = User.register(
            UserId.generate(),
            email,
            hashedPassword
        );

        // 4. Save
        userRepository.save(user);

        return UserResponse.from(user);
    }

    @Transactional
    public void inviteUserToTenant(String tenantIdStr, String emailStr, String roleName) {
        // 1. Validate Tenant Exists
        TenantId tenantId = TenantId.fromString(tenantIdStr);
        if (tenantRepository.findById(tenantId).isEmpty()) {
            throw new IllegalArgumentException("Tenant not found: " + tenantIdStr);
        }

        // 2. Find User (For this step, we assume User must already exist)
        Email email = new Email(emailStr);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + emailStr));

        // 3. Create Role Value Object
        Role role = new Role(roleName);

        // 4. Update Domain Model
        user.addToTenant(tenantId, role);

        // 5. Save (updates the user aggregate)
        userRepository.save(user);
    }
}