package hyper.run.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * SNS Topic ARN을 Map으로 관리하는 Properties 클래스
 */
@Component
@ConditionalOnProperty(name = "cloud.aws.sns.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "cloud.aws.sns")
@Getter
@Setter
public class SnsTopicProperties {

    private Map<String, String> topic = new HashMap<>();

    public String getTopicArn(String topicType) {
        String topicArn = topic.get(topicType);
        if (topicArn == null || topicArn.isEmpty()) {
            throw new IllegalArgumentException("Topic not configured for type: " + topicType);
        }
        return topicArn;
    }
}
