package pl.jakubsiekiera.iam.application.dto;

import pl.jakubsiekiera.iam.domain.model.user.User;
import java.util.UUID;

public record UserResponse(UUID id, String email) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId().value(),
            user.getEmail().value()
        );
    }
}