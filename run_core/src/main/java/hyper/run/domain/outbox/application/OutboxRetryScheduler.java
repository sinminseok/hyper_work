package hyper.run.domain.outbox.application;

import hyper.run.domain.outbox.entity.OutboxEvent;
import hyper.run.domain.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SNS 발행 실패한 OutboxEvent를 주기적으로 재발행하는 스케줄러
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRetryScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final List<OutboxEventPublishProcessor> processors;

    private static final int MAX_RETRY_COUNT = 3;

    @Scheduled(fixedDelay = 60000) // 1분마다 실행
    @Transactional
    public void retryFailedSnsPublish() {
        List<OutboxEvent> failedEvents = outboxEventRepository.findSnsPublishFailedEvents(MAX_RETRY_COUNT);

        if (failedEvents.isEmpty()) {
            return;
        }

        log.info("Found {} SNS publish failed events to retry", failedEvents.size());

        for (OutboxEvent event : failedEvents) {
            retryPublish(event);
        }
    }

    private void retryPublish(OutboxEvent event) {
        processors.stream()
                .filter(processor -> processor.getType().equals(event.getType()))
                .findAny()
                .ifPresentOrElse(
                        processor -> {
                            try {
                                processor.publish(event.getId(), event.getData());
                                event.markPublishedToQueue();
                                log.info("Successfully retried SNS publish. eventId: {}, type: {}", event.getId(), event.getType());
                            } catch (Exception e) {
                                event.incrementRetryCount();
                                log.error("Retry failed for eventId: {}, retryCount: {}", event.getId(), event.getRetryCount(), e);
                            }
                        },
                        () -> log.error("No processor found for event type: {}", event.getType())
                );
    }
}
