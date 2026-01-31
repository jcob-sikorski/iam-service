package pl.jakubsiekiera.iam.domain.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

class EmailTest {

    @ParameterizedTest
    @ValueSource(strings = {"test@example.com", "user.name@domain.co.uk", "user+tag@gmail.com"})
    @DisplayName("Should create email for valid formats")
    void shouldCreateValidEmail(String validEmail) {
        Email email = new Email(validEmail);
        assertThat(email.value()).isEqualTo(validEmail);
    }

    @ParameterizedTest
    @ValueSource(strings = {"plainaddress", "@missinguser.com", "missingdomain@", "user@.com"})
    @DisplayName("Should throw exception for invalid formats")
    void shouldThrowOnInvalidEmail(String invalidEmail) {
        assertThatThrownBy(() -> new Email(invalidEmail))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid email format");
    }
}