package pl.jakubsiekiera.iam.domain.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the {@link Email} Value Object.
 * Ensures that only syntactically valid email addresses can be instantiated.
 */
class EmailTest {

    /**
     * Verifies that the Email object is correctly initialized when provided with 
     * standard, widely accepted email formats.
     * * @param validEmail a string representing a correctly formatted email address
     */
    @ParameterizedTest
    @ValueSource(strings = {
        "test@example.com",          // Standard format
        "user.name@domain.co.uk",    // Subdomains and multiple dots
        "user+tag@gmail.com"         // Sub-addressing/tagging support
    })
    @DisplayName("Should create email for valid formats")
    void shouldCreateValidEmail(String validEmail) {
        // When: Creating a new Email instance
        Email email = new Email(validEmail);

        // Then: The internal value should match the input
        assertThat(email.value()).isEqualTo(validEmail);
    }

    /**
     * Verifies that the Email constructor enforces validation and prevents 
     * the creation of objects with malformed strings.
     * * @param invalidEmail a string representing an incorrect email format
     */
    @ParameterizedTest
    @ValueSource(strings = {
        "plainaddress",    // Missing @ and domain
        "@missinguser.com", // Missing local part
        "missingdomain@",   // Missing domain part
        "user@.com"         // Invalid domain start
    })
    @DisplayName("Should throw exception for invalid formats")
    void shouldThrowOnInvalidEmail(String invalidEmail) {
        // Then: Attempting to instantiate should result in an IllegalArgumentException
        assertThatThrownBy(() -> new Email(invalidEmail))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid email format");
    }
}