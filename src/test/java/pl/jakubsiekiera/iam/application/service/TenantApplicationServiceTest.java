// Defines the package location for this test class
package pl.jakubsiekiera.iam.application.service;

// Imports for JUnit 5 lifecycle and metadata annotations
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

// Imports for Mockito functionality to handle mocks and capture arguments
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Spring framework import for handling domain events
import org.springframework.context.ApplicationEventPublisher;

// Project-specific Data Transfer Objects (DTOs)
import pl.jakubsiekiera.iam.application.dto.RegisterTenantCommand;
import pl.jakubsiekiera.iam.application.dto.TenantResponse;

// Project-specific Domain models and events
import pl.jakubsiekiera.iam.domain.event.TenantRegisteredEvent;
import pl.jakubsiekiera.iam.domain.model.tenant.Tenant;
import pl.jakubsiekiera.iam.domain.model.tenant.TenantStatus;

// Project-specific Repository interface
import pl.jakubsiekiera.iam.domain.repository.TenantRepository;

// Static imports for AssertJ (fluent assertions) and Mockito (mocking behavior)
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TenantApplicationService}.
 * Verifies tenant registration logic, persistence, and event notification.
 */
// Integrates Mockito with JUnit 5 to enable @Mock annotations
@ExtendWith(MockitoExtension.class)
class TenantApplicationServiceTest {

    // Creates a mock instance of the repository to simulate database access
    @Mock 
    private TenantRepository tenantRepository;
    
    // Creates a mock instance of the publisher to simulate event broadcasting
    @Mock 
    private ApplicationEventPublisher eventPublisher;

    // The actual class under test
    private TenantApplicationService service;

    // Method to run before every single test case
    @BeforeEach
    void setUp() {
        // Manually injects the mocked dependencies into the service instance
        service = new TenantApplicationService(tenantRepository, eventPublisher);
    }

    // Indicates this is a test method
    @Test
    // Provides a human-readable description for the test report
    @DisplayName("Should register tenant, save to repo, and publish event")
    void shouldRegisterTenant() {
        // --- Arrange: Set up the input data and mock expectations ---
        
        // Local variables for test inputs
        String name = "Mega Corp";
        String email = "admin@megacorp.com"; 
        // Creating the input command DTO
        RegisterTenantCommand command = new RegisterTenantCommand(name, email);
        
        // Define mock behavior: return false when checking if the name exists
        when(tenantRepository.existsByName(name)).thenReturn(false);

        // --- Act: Execute the registration logic ---
        
        // Calls the method being tested and stores the result
        TenantResponse response = service.registerTenant(command);

        // --- Assert: Verify the tenant was persisted correctly ---
        
        // Creates a tool to 'catch' the Tenant object passed to the save method
        ArgumentCaptor<Tenant> tenantCaptor = ArgumentCaptor.forClass(Tenant.class);
        // Verifies the save method was called and grabs the object passed to it
        verify(tenantRepository).save(tenantCaptor.capture());
        
        // Extracts the captured tenant to inspect its properties
        Tenant savedTenant = tenantCaptor.getValue();
        // Assert that the name matches our input
        assertThat(savedTenant.getName()).isEqualTo(name);
        // Assert that the tenant defaults to an ACTIVE status
        assertThat(savedTenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);

        // --- Assert: Verify the Domain Event was published for downstream consumers ---
        
        // Creates a tool to 'catch' the Event object published
        ArgumentCaptor<TenantRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(TenantRegisteredEvent.class);
        // Verifies the publisher was triggered with the event
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        // Extracts the captured event to check its details
        TenantRegisteredEvent event = eventCaptor.getValue();
        // Verifies the event contains the correct Tenant ID and Name
        assertThat(event.tenantId()).isEqualTo(savedTenant.getId());
        assertThat(event.name()).isEqualTo(name);

        // Final check that the service actually returned a response object
        assertThat(response).isNotNull();
    }

    // Indicates this is a test method
    @Test
    // Provides a human-readable description for the failure scenario
    @DisplayName("Should throw exception if tenant name already exists")
    void shouldFailOnDuplicateName() {
        // --- Arrange: Prepare a command with a name that already exists in the system ---
        
        String name = "Mega Corp";
        RegisterTenantCommand command = new RegisterTenantCommand(name, "duplicate@test.com");
        
        // Define mock behavior: return true to simulate a name collision in the DB
        when(tenantRepository.existsByName(name)).thenReturn(true);

        // --- Act & Assert: Verify that an exception is thrown and side effects are avoided ---
        
        // Asserts that calling the service method triggers an IllegalArgumentException
        assertThatThrownBy(() -> service.registerTenant(command))
            .isInstanceOf(IllegalArgumentException.class) // Check exception type
            .hasMessageContaining("already exists");      // Check exception message

        // Safety check: ensure the repository save method was never called
        verify(tenantRepository, never()).save(any());
        // Safety check: ensure no events were broadcasted since the action failed
        verify(eventPublisher, never()).publishEvent(any());
    }
}