package pl.jakubsiekiera.iam.infrastructure.observability;

import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Diagnostic component that executes immediately after the Spring application context starts.
 * Its purpose is to verify the health and configuration of the Micrometer Tracing / OpenTelemetry stack.
 * * This helps catch "silent failures" where spans are generated but never reach the collector.
 */
@Component
public class ObservabilityDiagnoser implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ObservabilityDiagnoser.class);

    /**
     * The Tracer bean is provided by the Micrometer Tracing bridge.
     * It is marked as optional (required = false) so the application doesn't 
     * crash if tracing is disabled or misconfigured.
     */
    @Autowired(required = false)
    private Tracer tracer;

    /**
     * Injects the OTLP (OpenTelemetry Protocol) endpoint defined in application properties.
     * Defaults to "UNKNOWN" if the property is missing.
     */
    @Value("${management.otlp.tracing.endpoint:UNKNOWN}")
    private String traceEndpoint;

    @Override
    public void run(String... args) {
        log.info("================ OBSERVABILITY DIAGNOSTIC ================");
        
        // 1. ENDPOINT VERIFICATION
        // Verifies if the application is pointing to the correct network location for the OTel Collector.
        log.info("1. Active Tracing Endpoint: {}", traceEndpoint);
        if (!traceEndpoint.contains("localhost") && !traceEndpoint.contains("127.0.0.1")) {
            log.warn("   ⚠️ WARNING: Endpoint is NOT localhost! Docker Compose might have overridden it.");
            log.warn("   If running outside of Docker, this might cause Connection Refused errors.");
        }

        // 2. BEAN EXISTENCE CHECK
        // If the Tracer bean is null, the Micrometer-OTel bridge is likely missing from the classpath.
        if (tracer == null) {
            log.error("2. ❌ Tracer Bean is MISSING! Auto-configuration failed.");
            log.error("   ACTION REQUIRED: Check your pom.xml/build.gradle for:");
            log.error("   - 'io.micrometer:micrometer-tracing-bridge-otel'");
            log.error("   - 'io.opentelemetry:opentelemetry-exporter-otlp'");
        } else {
            log.info("2. ✅ Tracer Bean is PRESENT: {}", tracer.getClass().getName());
            
            // 3. MANUAL SPAN EMISSION (E2E TEST)
            // Creates a "fire-and-forget" span to trigger the exporter immediately.
            // This confirms that the exporter can serialize the data and reach the endpoint.
            log.info("3. 🚀 Sending TEST SPAN now...");
            try {
                // Creates a new span, starts it, and ends it immediately.
                tracer.nextSpan().name("startup-diagnostic-span").start().end();
                log.info("   (Check OTel Collector/Jaeger logs for 'startup-diagnostic-span')");
            } catch (Exception e) {
                log.error("   ❌ Failed to emit test span: {}", e.getMessage());
            }
        }
        log.info("==========================================================");
    }
}