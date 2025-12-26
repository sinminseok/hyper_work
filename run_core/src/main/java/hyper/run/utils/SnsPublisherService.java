package hyper.run.utils;

import hyper.run.config.SnsTopicProperties;
import hyper.run.common.enums.JobType;
import io.awspring.cloud.sns.core.SnsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * SNS 메시지 발행을 담당하는 공통 서비스
 * 다양한 타입의 이벤트를 SNS로 발행할 수 있습니다.
 */
@Service
@ConditionalOnProperty(name = "cloud.aws.sns.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class SnsPublisherService {

    private final SnsTemplate snsTemplate;
    private final SnsTopicProperties snsTopicProperties;

    public void publish(JobType jobType, Object message, String eventId) {
        try {
            String topicArn = snsTopicProperties.getTopicArn(jobType.getName());
            snsTemplate.sendNotification(topicArn, message, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish message to SNS", e);
        }
    }
}
