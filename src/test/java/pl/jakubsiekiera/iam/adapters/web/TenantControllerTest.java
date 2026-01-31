package pl.jakubsiekiera.iam.adapters.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.jakubsiekiera.iam.application.dto.InviteUserCommand;
import pl.jakubsiekiera.iam.application.dto.RegisterTenantCommand;
import pl.jakubsiekiera.iam.application.dto.TenantDetails;
import pl.jakubsiekiera.iam.application.dto.TenantResponse;
import pl.jakubsiekiera.iam.application.query.TenantQueryService;
import pl.jakubsiekiera.iam.application.service.TenantApplicationService;
import pl.jakubsiekiera.iam.application.service.UserApplicationService;
import pl.jakubsiekiera.iam.domain.model.tenant.TenantStatus;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the Web Adapter (Controller) layer using Spring's MockMvc.
 * * @WebMvcTest: Orchestrates a "Sliced" test context. It ignores full @SpringBootApplication 
 * scanning and only initializes beans related to the Web layer (Controllers, ExceptionHandlers).
 * * @AutoConfigureMockMvc: Configures the MockMvc instance. 'addFilters = false' is used 
 * to bypass Spring Security, focusing strictly on endpoint logic and JSON mapping.
 */
@WebMvcTest(controllers = TenantController.class)
@AutoConfigureMockMvc(addFilters = false)
class TenantControllerTest {

    /**
     * MockMvc allows us to execute HTTP-like requests against the Controller 
     * without the overhead of a running Netty/Tomcat server.
     */
    @Autowired 
    private MockMvc mockMvc;

    /**
     * ObjectMapper is used to convert Java DTOs into JSON strings for POST/PUT payloads.
     * .findAndRegisterModules() ensures support for Java 8 Date/Time (Instant).
     */
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    /**
     * @MockitoBean (introduced in Spring Boot 3.4) replaces @MockBean.
     * It automatically creates a Mockito mock and puts it into the Spring ApplicationContext.
     * This decouples the Controller from the actual business logic implementation.
     */
    @MockitoBean
    private TenantApplicationService tenantService;

    @MockitoBean
    private UserApplicationService userService;

    /**
     * Separating Query and Command services indicates a CQRS (Command Query Responsibility Segregation) 
     * architecture within the Application layer.
     */
    @MockitoBean
    private TenantQueryService tenantQueryService;

    @Test
    @DisplayName("POST /api/v1/tenants - Should return 201 Created and Location header on success")
    void shouldRegisterTenant() throws Exception {
        // --- Arrange ---
        // 1. Prepare input data (Command)
        var command = new RegisterTenantCommand("Acme Corp", "admin@acme.com");
        var responseId = UUID.randomUUID();
        
        // 2. Prepare expected output data (DTO)
        var responseDto = new TenantResponse(
            responseId, 
            "Acme Corp", 
            TenantStatus.ACTIVE.name(), 
            Instant.now()
        );

        // 3. Stub the service: When the controller calls 'registerTenant', return our DTO
        when(tenantService.registerTenant(any(RegisterTenantCommand.class)))
                .thenReturn(responseDto);

        // --- Act & Assert ---
        mockMvc.perform(post("/api/v1/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                
                // Assert: The status should be 201 (Created)
                .andExpect(status().isCreated())
                
                // Assert: REST standards recommend returning the URI of the new resource in the Location header
                .andExpect(header().string("Location", "/api/v1/tenants/" + responseId))
                
                // Assert: Validate the JSON body content using JsonPath
                .andExpect(jsonPath("$.id").value(responseId.toString()))
                .andExpect(jsonPath("$.name").value("Acme Corp"));
    }

    @Test
    @DisplayName("POST /api/v1/tenants - Should return 409 Conflict if tenant already exists")
    void shouldReturnConflictWhenServiceThrowsException() throws Exception {
        // --- Arrange ---
        var command = new RegisterTenantCommand("Acme Corp", "admin@acme.com");
        
        // Stub the service to throw an exception, simulating a domain constraint violation
        when(tenantService.registerTenant(any()))
                .thenThrow(new IllegalArgumentException("Tenant already exists"));

        // --- Act & Assert ---
        mockMvc.perform(post("/api/v1/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                
                // Assert: The GlobalExceptionHandler should catch IllegalArgumentException 
                // and return a 409 Conflict status.
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Tenant already exists"));
    }

    @Test
    @DisplayName("POST /api/v1/tenants/{id}/users - Should call User Service to invite a member")
    void shouldInviteUser() throws Exception {
        // --- Arrange ---
        var tenantId = UUID.randomUUID().toString();
        var command = new InviteUserCommand("new@user.com", "MEMBER");

        // --- Act ---
        mockMvc.perform(post("/api/v1/tenants/{id}/users", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                // Assert: Status check
                .andExpect(status().isOk());

        // --- Verification ---
        // Since 'inviteUserToTenant' is likely a void method, we verify the interaction 
        // with the mock to ensure the controller passed the correct arguments.
        verify(userService).inviteUserToTenant(tenantId, "new@user.com", "MEMBER");
    }

    @Test
    @DisplayName("GET /api/v1/tenants/{id} - Should return tenant details for the Read Model")
    void shouldGetTenantDetails() throws Exception {
        // --- Arrange ---
        UUID tenantId = UUID.randomUUID();
        
        // Construct the Read-Side DTO
        TenantDetails details = new TenantDetails(
            tenantId, 
            "Acme", 
            TenantStatus.ACTIVE.name(), 
            Instant.now(), 
            Collections.emptyList()
        );
        
        // Stubbing the Query service
        when(tenantQueryService.getTenantDetails(tenantId)).thenReturn(details);

        // --- Act & Assert ---
        mockMvc.perform(get("/api/v1/tenants/{id}", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Acme"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}