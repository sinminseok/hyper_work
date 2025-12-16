package hyper.run.domain.outbox.application;


import hyper.run.domain.outbox.entity.OutboxCommittedEvent;
import hyper.run.domain.outbox.entity.OutboxEvent;
import hyper.run.domain.outbox.entity.OutboxEventData;
import hyper.run.domain.outbox.entity.OutboxEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {
    private final List<OutboxEventPublishProcessor> processors;

    public void publish(OutboxEvent event) {
        publishEvent(event.getType(), event.getId(), event.getData());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(OutboxCommittedEvent event) {
        publishEvent(event.getType(), event.getOutboxEventId(), event.getData());
    }

    private void publishEvent(OutboxEventType eventType, String eventId, OutboxEventData eventData) {
        processors.stream()
                .filter(processor -> processor.getType().equals(eventType))
                .findAny()
                .ifPresentOrElse(
                        processor -> {
                            try {
                                processor.publish(eventId, eventData);
                            } catch (Exception e) {
                                log.error("Failed to publish event. eventId : {}", eventId, e);
                            }
                        },
                        () -> log.error("No processor found for event type: {}", eventType)
                );
    }
}
