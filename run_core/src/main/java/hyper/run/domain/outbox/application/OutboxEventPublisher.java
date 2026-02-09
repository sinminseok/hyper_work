package hyper.run.domain.outbox.application;

import hyper.run.common.job.JobEventPayload;
import hyper.run.domain.outbox.entity.OutboxCommittedEvent;
import hyper.run.domain.outbox.entity.OutboxEvent;
import hyper.run.common.enums.JobType;
import hyper.run.domain.outbox.repository.OutboxEventRepository;
import hyper.run.exception.ErrorMessages;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

import static hyper.run.exception.ErrorMessages.NOT_EXIST_OUTBOX_EVENT_ID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {
    private final List<OutboxEventPublishProcessor> processors;
    private final OutboxEventRepository outboxEventRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void publish(OutboxCommittedEvent event) {
        publishEvent(event.getType(), event.getOutboxEventId(), event.getData());
    }

    private void publishEvent(JobType eventType, String eventId, JobEventPayload eventData) {
        processors.stream()
                .filter(processor -> processor.getType().equals(eventType))
                .findAny()
                .ifPresentOrElse(
                        processor -> {
                            try {
                                processor.publish(eventId, eventData);
                                markPublishedToQueue(eventId);
                            } catch (Exception e) {
                                log.error("Failed to publish event. eventId: {}", eventId, e);
                            }
                        },
                        () -> log.error("No processor found for event type: {}", eventType)
                );
    }

    private void markPublishedToQueue(String eventId) {
        try {
            OutboxEvent event = OptionalUtil.getOrElseThrow(outboxEventRepository.findById(eventId), NOT_EXIST_OUTBOX_EVENT_ID);
            event.markPublishedToQueue();
            outboxEventRepository.save(event);
        } catch (Exception e) {
            log.error("Failed to mark publishedToQueue. eventId: {}", eventId, e);
        }
    }
}
