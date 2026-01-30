package pl.jakubsiekiera.iam.infrastructure.identity;

import pl.jakubsiekiera.iam.domain.service.IdentityProvider;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Implementation of the IdentityProvider interface using Keycloak as the provider.
 * This service handles user management tasks within the specified Keycloak realm.
 */
@Service
@RequiredArgsConstructor
public class KeycloakIdentityProvider implements IdentityProvider {

    // Keycloak admin client used to interact with the Keycloak REST API
    private final Keycloak keycloak;

    // The specific realm name where users are managed, injected from application properties
    @Value("${keycloak.realm}")
    private String realm;

    /**
     * Registers a new user in Keycloak with the provided credentials.
     * * @return The unique ID of the newly created user.
     * @throws RuntimeException if the user creation fails.
     */
    @Override
    public String registerUser(String username, String email, String password) {
        
        // 1. Prepare the User Metadata
        // We define the basic profile information for the new account.
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setEnabled(true);
        user.setEmailVerified(true);

        // 2. Prepare the User Credentials
        // We define the password settings, ensuring it's set as a non-temporary password.
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        user.setFirstName(username); // Keycloak requires this by default
        user.setLastName("User");    // Keycloak requires this by default

        // Link the credentials to the user representation
        user.setCredentials(List.of(credential));

        // 3. Access the Keycloak Users Resource
        // Navigates the Keycloak API to the specific realm's user management endpoint.
        UsersResource usersResource = keycloak.realm(realm).users();
        
        // 4. Execute the Registration Request
        // We use a try-with-resources block to ensure the HTTP response is closed properly.
        try (Response response = usersResource.create(user)) {
            
            // Keycloak returns HTTP 201 (Created) upon successful user creation
            if (response.getStatus() == 201) {
                
                // Extract the User ID from the 'Location' header.
                // The header usually looks like: .../auth/admin/realms/myrealm/users/{uuid}
                String path = response.getLocation().getPath();
                return path.substring(path.lastIndexOf("/") + 1);
                
            } else {
                // If the status isn't 201, something went wrong (e.g., user already exists, invalid data).
                // In production, you'd ideally parse response.getEntity() for more detail.
                throw new RuntimeException("Failed to create user in Keycloak. Status: " + response.getStatus());
            }
        }
    }
}