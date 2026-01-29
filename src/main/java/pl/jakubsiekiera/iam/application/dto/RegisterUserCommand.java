package pl.jakubsiekiera.iam.application.dto;

public record RegisterUserCommand(
    String email, 
    String keycloakId // Require the ID from the IdP
) {}