package hyper.run.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * SQS Queue 이름을 Map으로 관리하는 Properties 클래스
 *
 * application.yml에서 아래와 같이 설정:
 * cloud:
 *   aws:
 *     sqs:
 *       queue:
 *         payment-created: payment-created-queue
 *         game-finished: game-finished-queue
 *         새로운큐: 새로운큐이름
 *
 * 새로운 큐를 추가할 때 application.yml만 수정하면 됨.
 * 코드 변경 불필요!
 */
@Component
@ConditionalOnProperty(name = "cloud.aws.sqs.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "cloud.aws.sqs")
@Getter
@Setter
public class SqsQueueProperties {

    /**
     * Queue 이름 맵
     * Key: queue type (예: "payment-created", "game-finished")
     * Value: 실제 SQS queue 이름
     */
    private Map<String, String> queue = new HashMap<>();

    /**
     * Queue 이름 조회
     *
     * @param queueType queue type (예: "payment-created")
     * @return 실제 SQS queue 이름
     * @throws IllegalArgumentException queue type이 존재하지 않는 경우
     */
    public String getQueueName(String queueType) {
        String queueName = queue.get(queueType);
        if (queueName == null || queueName.isEmpty()) {
            throw new IllegalArgumentException("Queue not configured for type: " + queueType);
        }
        return queueName;
    }
}
