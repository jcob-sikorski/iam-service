package pl.jakubsiekiera.iam.application.listener;

import pl.jakubsiekiera.iam.domain.event.TenantRegisteredEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class WelcomeEmailListener {

    @EventListener
    public void handle(TenantRegisteredEvent event) {
        // In a real app, this would call an EmailService
        System.out.println("--------------------------------------------------");
        System.out.println(" [NOTIFICATION SERVICE] Sending Welcome Email");
        System.out.println(" To Tenant: " + event.name());
        System.out.println(" ID: " + event.tenantId().value());
        System.out.println("--------------------------------------------------");
    }
}