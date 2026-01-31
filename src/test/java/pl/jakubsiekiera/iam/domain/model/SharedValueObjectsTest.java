package pl.jakubsiekiera.iam.domain.model;

import org.junit.jupiter.api.Test;
import pl.jakubsiekiera.iam.domain.model.tenant.TenantId;
import pl.jakubsiekiera.iam.domain.model.user.Role;
import pl.jakubsiekiera.iam.domain.model.user.UserId;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class SharedValueObjectsTest {

    @Test
    void tenantIdShouldNotBeNull() {
        assertThatThrownBy(() -> new TenantId(null))
            .isInstanceOf(IllegalArgumentException.class);
            
        assertThat(TenantId.fromString(UUID.randomUUID().toString())).isNotNull();
    }

    @Test
    void userIdShouldNotBeNull() {
        assertThatThrownBy(() -> new UserId(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void roleShouldRequireName() {
        assertThatThrownBy(() -> new Role(null))
            .isInstanceOf(IllegalArgumentException.class);
            
        assertThat(Role.ADMIN.name()).isEqualTo("ADMIN");
    }
}