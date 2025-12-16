package hyper.run.domain.outbox.application;

import hyper.run.domain.outbox.entity.OutboxEventData;
import hyper.run.domain.outbox.entity.OutboxEventType;
import org.springframework.stereotype.Component;

@Component
public abstract class OutboxEventPublishProcessor {
    protected abstract void publish(String eventId, OutboxEventData data);
    protected abstract OutboxEventType getType();

}
