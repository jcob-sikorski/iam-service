package pl.jakubsiekiera.iam.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TenantDetails(
    UUID id,
    String name,
    String status,
    Instant creationDate,
    List<Member> members
) {}