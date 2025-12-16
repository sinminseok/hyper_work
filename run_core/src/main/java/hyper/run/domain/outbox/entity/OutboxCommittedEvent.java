package hyper.run.domain.outbox.entity;

import hyper.run.domain.outbox.entity.OutboxEventData;
import hyper.run.domain.outbox.entity.OutboxEventType;
import lombok.Getter;

@Getter
public class OutboxCommittedEvent {
    private final String outboxEventId;
    private final OutboxEventType type;
    private final OutboxEventData data;

    public OutboxCommittedEvent(String outboxEventId, OutboxEventType type, OutboxEventData data) {
        this.outboxEventId = outboxEventId;
        this.type = type;
        this.data = data;
    }
}
