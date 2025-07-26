package hyper.run.domain.user.dto.request;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "aligo")
@Data
public class AligoProperties {
    private String userId;
    private String apiKey;
    private String sender;
}