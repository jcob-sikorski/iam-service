package pl.jakubsiekiera.iam.adapters.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TestController serves as a simple diagnostic utility within the Identity and Access Management (IAM) 
 * web adapter layer. Its primary purpose is to verify application health and ensure that 
 * observability tools (like Sleuth, Zipkin, or OpenTelemetry) are correctly capturing traces.
 */
@RestController
public class TestController {

    // Initializing the SLF4J logger for this specific class to track request flow and debugging info.
    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    /**
     * Handles GET requests to the /api/ping endpoint.
     * This is an unauthenticated "heartbeat" endpoint used to check if the service is responsive.
     * * @return A simple string "pong" to confirm the server is up and reachable.
     */
    @GetMapping("/api/ping")
    public String ping() {
        /* * Logic: We log this event at the INFO level. 
         * In a distributed system, this log entry will typically be enriched with 
         * TraceID and SpanID by the underlying tracing library, allowing us to 
         * follow the request across microservices.
         */
        log.info("Ping request received - should generate a trace!");
        
        return "pong";
    }
}