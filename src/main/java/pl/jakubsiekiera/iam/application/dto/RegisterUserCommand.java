package pl.jakubsiekiera.iam.application.dto;

public record RegisterUserCommand(
    String email, 
    String password
) {}