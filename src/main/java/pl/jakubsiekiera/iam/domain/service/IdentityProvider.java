package pl.jakubsiekiera.iam.domain.service;

public interface IdentityProvider {
    String registerUser(String username, String email, String password);
}