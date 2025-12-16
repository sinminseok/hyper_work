package hyper.run.domain.sns;

import hyper.run.domain.outbox.entity.OutboxEventData;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OutboxEventSnsMessage {
    private String eventId;
    private OutboxEventData eventData;

    public OutboxEventSnsMessage(String eventId, OutboxEventData eventData) {
        this.eventId = eventId;
        this.eventData = eventData;
    }
}
