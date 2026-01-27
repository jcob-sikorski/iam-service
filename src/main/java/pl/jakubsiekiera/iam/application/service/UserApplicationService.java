package pl.jakubsiekiera.iam.application.service;

import pl.jakubsiekiera.iam.application.dto.RegisterUserCommand;
import pl.jakubsiekiera.iam.application.dto.UserResponse;
import pl.jakubsiekiera.iam.domain.model.user.Email;
import pl.jakubsiekiera.iam.domain.model.user.User;
import pl.jakubsiekiera.iam.domain.model.user.UserId;
import pl.jakubsiekiera.iam.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private final UserRepository userRepository;
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
}