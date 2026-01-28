package pl.jakubsiekiera.iam.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

/**
 * @Entity: Marks this class as a JPA entity, meaning it will be mapped to a database table.
 * @Table: Specifies the primary table name in the database ("tenants").
 * @Data: A Lombok annotation that generates Getters, Setters, equals(), hashCode(), and toString().
 */
@Entity
@Table(name = "tenants")
@Data 
public class TenantJpaEntity {
    
    /**
     * @Id: Marks this field as the primary key of the entity.
     * UUID: Used here for globally unique identifiers, often safer for distributed systems than auto-incrementing longs.
     */
    @Id
    private UUID id;

    /**
     * @Column: Configures the mapping for this field.
     * nullable = false: Ensures the 'name' column cannot be empty (NOT NULL constraint).
     * unique = true: Ensures no two tenants can have the exact same name.
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * @Column(nullable = false): Ensures a status is always assigned.
     * Note: Storing enums as Strings (ACTIVE, SUSPENDED) makes the database more readable 
     * compared to ordinals (0, 1), which can break if the enum order changes.
     */
    @Column(nullable = false)
    private String status; 

    /**
     * Instant: Represents a point in time in UTC. 
     * This is the modern Java best practice for storing timestamps without timezone ambiguity.
     */
    @Column(nullable = false)
    private Instant creationDate;
}