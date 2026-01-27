package pl.jakubsiekiera.iam.application.dto;

public record InviteUserCommand(
    String email, 
    String role
) {}