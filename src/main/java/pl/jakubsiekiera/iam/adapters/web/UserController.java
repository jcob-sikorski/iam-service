package pl.jakubsiekiera.iam.adapters.web;

import pl.jakubsiekiera.iam.application.dto.RegisterUserCommand;
import pl.jakubsiekiera.iam.application.dto.UserResponse;
import pl.jakubsiekiera.iam.application.service.UserApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserApplicationService userService;

    @PostMapping
    public ResponseEntity<UserResponse> register(@RequestBody RegisterUserCommand command) {
        UserResponse response = userService.registerUser(command);
        return ResponseEntity
                .created(URI.create("/api/v1/users/" + response.id()))
                .body(response);
    }
}