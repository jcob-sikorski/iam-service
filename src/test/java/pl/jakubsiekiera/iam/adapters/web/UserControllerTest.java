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
import pl.jakubsiekiera.iam.application.dto.RegisterUserCommand;
import pl.jakubsiekiera.iam.application.dto.UserResponse;
import pl.jakubsiekiera.iam.application.service.UserApplicationService;
import pl.jakubsiekiera.iam.domain.model.user.UserId;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit test for the Web Layer (UserController).
 * * @WebMvcTest slices the application context to include ONLY infrastructure relevant to Spring MVC.
 * It won't load the full @SpringBootTest, making it significantly faster.
 * * @AutoConfigureMockMvc(addFilters = false) ensures we can test the endpoint logic without
 * being blocked by Spring Security filters (JWT, CSRF, etc.).
 */
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    /**
     * MockMvc allows us to send "fake" HTTP requests to our controller and assert responses
     * without the overhead of starting a real Tomcat/Netty server.
     */
    @Autowired 
    private MockMvc mockMvc;

    /**
     * Jackson ObjectMapper used to serialize Request DTOs into JSON strings.
     * .findAndRegisterModules() ensures support for Java 8 Dates and Optional types if needed.
     */
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    /**
     * The @MockitoBean annotation (introduced in Spring Boot 3.4) mocks the application service.
     * This ensures the test is a true unit test for the Controller, isolating it from 
     * database or business logic failures in the service layer.
     */
    @MockitoBean
    private UserApplicationService userService;

    /**
     * Test Case: Successful User Registration.
     * This verifies:
     * 1. The POST endpoint is mapped correctly to /api/v1/users.
     * 2. JSON Request Body is correctly deserialized into a RegisterUserCommand.
     * 3. The Controller returns HTTP 201 Created.
     * 4. The Location header is built correctly using the returned ID.
     */
    @Test
    @DisplayName("POST /api/v1/users - Should register user and return 201 Created")
    void shouldRegisterUser() throws Exception {
        // --- 1. Arrange (Setup) ---
        // Create the input command representing the JSON payload
        var command = new RegisterUserCommand("john", "john@test.com", "password");
        
        // Generate a mock response. We extract .value() (the UUID/String) 
        // to match what the UserResponse record expects.
        var response = new UserResponse(UserId.generate().value(), "john@test.com");

        // Stubbing: Instruct the mock service to return our predefined response 
        // when ANY registration command is passed to it.
        when(userService.registerUser(any(RegisterUserCommand.class))).thenReturn(response);

        // --- 2. Act (Execution) ---
        // Perform the POST request to the controller endpoint
        var resultActions = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON) // Set request header
                        .content(objectMapper.writeValueAsString(command))); // Serialize object to JSON body
                
        // --- 3. Assert (Verification) ---
        resultActions
                // Verify the HTTP Status Code is 201 (Created)
                .andExpect(status().isCreated())
                
                // Verify the Location header (standard for RESTful creation) matches our ID
                .andExpect(header().string("Location", "/api/v1/users/" + response.id()))
                
                // Use JsonPath to verify the content of the JSON response body
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }
}