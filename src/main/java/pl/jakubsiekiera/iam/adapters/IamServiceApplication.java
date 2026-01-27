package pl.jakubsiekiera.iam.adapters;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(
    exclude = {DataSourceAutoConfiguration.class},
    scanBasePackages = "pl.jakubsiekiera.iam"
)
public class IamServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IamServiceApplication.class, args);
    }
}