package pl.jakubsiekiera.iam.application.dto;

public record RegisterUserCommand(
    String username,
    String email,
    String password
) {}