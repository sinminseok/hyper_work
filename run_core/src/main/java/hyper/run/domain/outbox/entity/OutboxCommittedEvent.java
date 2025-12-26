package hyper.run.domain.outbox.entity;

import hyper.run.common.job.JobEventPayload;
import hyper.run.common.enums.JobType;
import lombok.Getter;

@Getter
public class OutboxCommittedEvent {
    private final String outboxEventId;
    private final JobType type;
    private final JobEventPayload data;

    public OutboxCommittedEvent(String outboxEventId, JobType type, JobEventPayload data) {
        this.outboxEventId = outboxEventId;
        this.type = type;
        this.data = data;
    }
}
