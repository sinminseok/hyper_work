package hyper.run.config;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "hyper.run.domain")
public class JpaConfig {
}
