package pl.jakubsiekiera.iam.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

/**
 * @Entity: Marks this class as a database-backed object.
 * @Table: Explicitly maps this class to the "user_memberships" table in SQL.
 * @Data: A Lombok annotation that automatically generates Getters, Setters, 
 * equals(), hashCode(), and toString() methods at compile time.
 */
@Entity
@Table(name = "user_memberships")
@Data
public class UserMembershipJpaEntity {

    /**
     * @Id: Marks this field as the Primary Key.
     * @GeneratedValue: Instructs Hibernate to automatically generate a unique 
     * UUID for every new row inserted into the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * @ManyToOne: Defines a relationship where many memberships can belong to one User.
     * fetch = FetchType.LAZY: Performance optimization. The 'user' data is only 
     * loaded from the DB if you actually call user.getName() (on-demand loading).
     * @JoinColumn: Specifies 'user_id' as the Foreign Key column in this table.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    /**
     * Identifies which Tenant (organization) this membership belongs to.
     * nullable = false ensures this record cannot exist without a tenant assignment.
     */
    @Column(nullable = false)
    private UUID tenantId;

    /**
     * Stores user permissions as a simple String.
     * Note: While simple, this often requires manual parsing (e.g., .split(",")) 
     * when checking permissions in the application logic.
     */
    @Column(nullable = false)
    private String roles; 
}