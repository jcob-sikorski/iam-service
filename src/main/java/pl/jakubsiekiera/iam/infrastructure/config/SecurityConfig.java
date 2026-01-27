package pl.jakubsiekiera.iam.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @Configuration: Tells the Spring "Factory" that this class contains recipes (beans).
 * @EnableWebSecurity: Activates the Spring Security infrastructure and looks for a SecurityFilterChain bean.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * @Bean: This method is a factory "recipe." Spring runs this once at startup,
     * takes the returned SecurityFilterChain, and stores it in the Application Context (the "vault").
     * * @param http: This is Dependency Injection. Spring provides this HttpSecurity builder 
     * automatically so we can "draw" our security rules onto it.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF: Since this is likely a stateless API, we disable 
            // Cross-Site Request Forgery protection to allow POST requests from external clients.
            .csrf(csrf -> csrf.disable())
            
            // 2. Configure endpoint rules: This defines the "Security Gauntlet."
            .authorizeHttpRequests(auth -> auth
                // Public Door: Specifically allows any unauthenticated user to POST to /tenants (Sign-up).
                .requestMatchers(HttpMethod.POST, "/api/v1/tenants", "/api/v1/users").permitAll()
                
                // Locked Doors: For any other URL, Spring will check if the user is logged in. 
                // If not, it blocks the request before it reaches any Controller.
                .anyRequest().authenticated()
            );

        // .build() converts our configuration into the actual FilterChain object 
        // that will sit in front of our application to guard it.
        return http.build();
    }

    /**
     * PasswordEncoder: This bean tells Spring how to hash and verify passwords.
     * BCrypt is the current industry standard for secure password storage.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}