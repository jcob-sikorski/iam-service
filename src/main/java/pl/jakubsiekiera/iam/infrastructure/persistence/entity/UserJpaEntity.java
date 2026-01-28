package pl.jakubsiekiera.iam.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

/**
 * @Entity: Maps this class to a database row.
 * @Table: Explicitly names the table "users". 
 * Note: In some SQL dialects (like PostgreSQL), "user" is a reserved keyword, 
 * so naming the table "users" (plural) is a best practice to avoid syntax errors.
 */
@Entity
@Table(name = "users")
@Data
public class UserJpaEntity {

    /**
     * @Id: The primary key. 
     * Using UUID ensures that user IDs are non-guessable and unique across microservices.
     */
    @Id
    private UUID id;

    /**
     * @Column(nullable = false, unique = true): 
     * Ensures every user has an email and that no two users share the same one.
     * This acts as the "identity" for authentication.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * passwordHash: NEVER store plain-text passwords.
     * This field stores the salted hash (e.g., BCrypt or Argon2).
     * It is marked 'nullable = false' because every user must have a credential.
     */
    @Column(nullable = false)
    private String passwordHash;

    /* * FUTURE SCALABILITY NOTES:
     * 1. Roles: If you choose @ElementCollection, JPA will create a separate join table.
     * 2. JSONB: If using PostgreSQL, you could store roles in a single column for speed.
     * 3. Relationships: Eventually, you might link this to TenantJpaEntity via a 
     * 'memberships' table to support multi-tenancy.
     */
}