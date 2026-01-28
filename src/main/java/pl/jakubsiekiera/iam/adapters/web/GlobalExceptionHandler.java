package pl.jakubsiekiera.iam.adapters.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/**
 * Global interceptor for exceptions thrown by any @RestController.
 * This ensures a consistent API error response format across the application.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles IllegalArgumentException, which often occurs during domain validation
     * (e.g., trying to register a user that already exists).
     * * @param ex The caught exception
     * @return A structured JSON response with a 409 Conflict status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        // We map the exception to a 409 Conflict. 
        // Note: Use 400 Bad Request if the error is purely syntactical.
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "Conflict",
                        "message", ex.getMessage(),
                        "timestamp", Instant.now()
                ));
    }
    
    // Future Tip: Add a @ExceptionHandler(MethodArgumentNotValidException.class) 
    // here to handle @Valid annotation failures from your DTOs.
}