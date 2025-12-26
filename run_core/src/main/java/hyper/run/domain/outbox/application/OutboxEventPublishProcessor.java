package hyper.run.domain.outbox.application;

import hyper.run.common.job.JobEventPayload;
import hyper.run.common.enums.JobType;
import org.springframework.stereotype.Component;

@Component
public abstract class OutboxEventPublishProcessor {
    protected abstract void publish(String eventId, JobEventPayload data);
    protected abstract JobType getType();
}
