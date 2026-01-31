package pl.jakubsiekiera.iam;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration tests for the IamService application.
 * * Note: Currently disabled in CI/CD because it requires a running 
 * database and keycloak instances that haven't been provisioned yet.
 */
@Disabled("Skipping integration test until DB is configured")
@SpringBootTest
class IamServiceApplicationTests {

    /**
     * Standard Spring Boot "sanity check" test.
     * It ensures that the Spring ApplicationContext can start successfully.
     * If this fails, there is likely a configuration error or missing dependency.
     */
    @Test
    void contextLoads() {
        // No assertions needed; the test passes if the context loads without throwing an exception.
    }

}