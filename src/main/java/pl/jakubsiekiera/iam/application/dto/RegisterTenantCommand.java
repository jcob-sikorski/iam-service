package pl.jakubsiekiera.iam.application.dto;

// A simple container for the data needed to register a tenant
public record RegisterTenantCommand(String name, String email) {
}