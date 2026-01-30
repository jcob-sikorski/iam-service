package pl.jakubsiekiera.iam.infrastructure.config;

import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to initialize the Keycloak Admin Client.
 * This bean allows the application to interact with Keycloak's REST API 
 * for administrative operations.
 */
@Configuration
public class KeycloakConfig {

    // The base URL of your Keycloak server (e.g., http://localhost:8081)
    @Value("${keycloak.auth-server-url:http://localhost:8081}")
    private String authServerUrl;

    // The specific realm your application will manage (though admin login often uses 'master')
    @Value("${keycloak.realm:saas-iam}")
    private String realm;

    // Credentials for the administrative user or service account
    // This user must have roles like 'manage-users' or 'realm-admin'
    @Value("${keycloak.admin.username:admin}") 
    private String adminUsername;

    @Value("${keycloak.admin.password:admin}")
    private String adminPassword;

    /**
     * Creates a singleton Bean of the Keycloak client.
     * Uses the Password Grant Type to authenticate as an admin user.
     */
    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                // We authenticate against the 'master' realm to get cross-realm admin rights
                .realm("master") 
                // Using standard username/password authentication for the admin client
                .grantType(OAuth2Constants.PASSWORD)
                .username(adminUsername)
                .password(adminPassword)
                // 'admin-cli' is the default Keycloak client ID for administrative tasks
                .clientId("admin-cli")
                // Custom HTTP client configuration to manage the connection pool size
                .resteasyClient(new ResteasyClientBuilderImpl()
                        .connectionPoolSize(10)
                        .build())
                .build();
    }
}