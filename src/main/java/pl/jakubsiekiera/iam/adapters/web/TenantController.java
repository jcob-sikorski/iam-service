package pl.jakubsiekiera.iam.adapters.web;

import pl.jakubsiekiera.iam.application.dto.RegisterTenantCommand;
import pl.jakubsiekiera.iam.application.dto.TenantResponse;
import pl.jakubsiekiera.iam.application.dto.InviteUserCommand;
import pl.jakubsiekiera.iam.application.service.UserApplicationService;
import pl.jakubsiekiera.iam.application.service.TenantApplicationService;
import pl.jakubsiekiera.iam.application.query.TenantQueryService;
import pl.jakubsiekiera.iam.application.dto.TenantDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final UserApplicationService userService;
    private final TenantApplicationService tenantService;
    private final TenantQueryService tenantQueryService;

    @PostMapping
    public ResponseEntity<TenantResponse> register(@RequestBody RegisterTenantCommand command) {
        TenantResponse response = tenantService.registerTenant(command);
        
        // Return 201 Created with Location header
        return ResponseEntity
                .created(URI.create("/api/v1/tenants/" + response.id()))
                .body(response);
    }

    @PostMapping("/{tenantId}/users")
    public ResponseEntity<Void> inviteUser(
            @PathVariable String tenantId,
            @RequestBody InviteUserCommand command) {
        
        userService.inviteUserToTenant(tenantId, command.email(), command.role());
        
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{tenantId}")
    public ResponseEntity<TenantDetails> getTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(tenantQueryService.getTenantDetails(tenantId));
    }
}