package pl.jakubsiekiera.iam.domain.model.user;

public record Role(String name) {
    public Role {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Role name cannot be empty");
    }
    
    // Standard roles
    public static final Role ADMIN = new Role("ADMIN");
    public static final Role MEMBER = new Role("MEMBER");
}