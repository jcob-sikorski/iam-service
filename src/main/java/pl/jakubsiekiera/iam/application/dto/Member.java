package pl.jakubsiekiera.iam.application.dto;

public record Member(
    String email,
    String roles // Comma separated string for simplicity, or List<String>
) {}