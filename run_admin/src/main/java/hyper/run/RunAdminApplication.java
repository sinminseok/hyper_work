package hyper.run;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RunAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(RunAdminApplication.class, args);
    }
}
