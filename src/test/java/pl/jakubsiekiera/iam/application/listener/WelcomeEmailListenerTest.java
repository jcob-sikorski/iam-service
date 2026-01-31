package pl.jakubsiekiera.iam.application.listener;

import pl.jakubsiekiera.iam.domain.event.TenantRegisteredEvent;
import pl.jakubsiekiera.iam.domain.model.tenant.TenantId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link WelcomeEmailListener}.
 * This class verifies that the listener correctly reacts to domain events
 * by "sending" an email (currently simulated via console output).
 */
@ExtendWith(MockitoExtension.class)
class WelcomeEmailListenerTest {

    @InjectMocks
    private WelcomeEmailListener welcomeEmailListener;

    // Captures System.out content to verify console logging
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    /**
     * Redirects System.out to our captor before each test to intercept 
     * printed notification messages.
     */
    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    /**
     * Restores the original System.out after each test to prevent side effects
     * in the test suite execution.
     */
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    /**
     * Verifies that when a TenantRegisteredEvent is received, the listener
     * outputs the correctly formatted welcome message.
     */
    @Test
    void shouldHandleTenantRegisteredEvent() {
        // Given: A valid registration event
        TenantId tenantId = TenantId.fromString("123e4567-e89b-12d3-a456-426614174000");
        String tenantName = "Test Tenant";
        Instant occurredOn = Instant.now();
        TenantRegisteredEvent event = new TenantRegisteredEvent(tenantId, tenantName, occurredOn);

        // When: The listener processes the event
        welcomeEmailListener.handle(event);

        // Then: The console output should contain the expected email simulation strings
        String output = outputStreamCaptor.toString();
        
        assertThat(output)
                .contains("[NOTIFICATION SERVICE] Sending Welcome Email")
                .contains("To Tenant: " + tenantName)
                .contains("ID: " + tenantId.value())
                .contains("--------------------------------------------------");
    }

    /**
     * Ensures the listener correctly maps and displays data from different tenants,
     * confirming the output isn't hardcoded.
     */
    @Test
    void shouldHandleEventWithDifferentTenantData() {
        // Given
        TenantId tenantId = TenantId.fromString("987e6543-e21b-98d7-b654-321456789000");
        String tenantName = "Another Company Ltd.";
        Instant occurredOn = Instant.now();
        TenantRegisteredEvent event = new TenantRegisteredEvent(tenantId, tenantName, occurredOn);

        // When
        welcomeEmailListener.handle(event);

        // Then
        String output = outputStreamCaptor.toString();
        
        assertThat(output)
                .contains("To Tenant: " + tenantName)
                .contains("ID: " + tenantId.value());
    }

    /**
     * Verifies that the notification logic handles special characters in tenant names
     * without breaking the output formatting.
     */
    @Test
    void shouldHandleEventWithSpecialCharactersInName() {
        // Given
        TenantId tenantId = TenantId.fromString("456e7890-a12b-34c5-d678-901234567890");
        String tenantName = "Test & Co. (™)";
        Instant occurredOn = Instant.now();
        TenantRegisteredEvent event = new TenantRegisteredEvent(tenantId, tenantName, occurredOn);

        // When
        welcomeEmailListener.handle(event);

        // Then
        String output = outputStreamCaptor.toString();
        
        assertThat(output)
                .contains("To Tenant: " + tenantName)
                .contains("ID: " + tenantId.value());
    }
}