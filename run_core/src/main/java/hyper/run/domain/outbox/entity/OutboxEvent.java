package hyper.run.domain.outbox.entity;

import hyper.run.common.job.JobEventPayload;
import hyper.run.common.enums.JobType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.Instant;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent extends AbstractAggregateRoot<OutboxEvent> {

    @Id
    @Column(name = "outbox_event_id")
    private String id;

    private JobType type;

    private boolean isPublished;

    private long createdAt;

    private int retryCount;

    private Long lastRetryAt;

    private Boolean publishedToQueue;

    private Long publishedToQueueAt;

    @Convert(converter = JobEventPayload.JobEventPayloadConverter.class)
    private JobEventPayload data;


    public OutboxEvent(JobType type, JobEventPayload data) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.isPublished = false;
        this.retryCount = 0;
        this.lastRetryAt = null;
        this.data = data;
        registerEvent(new OutboxCommittedEvent(id, type, data));
    }


    public String getId() {
        return id;
    }

    public JobType getType() {
        return type;
    }

    public JobEventPayload getData() {
        return data;
    }

    public boolean isPublished() {
        return this.isPublished;
    }

    public void publish() {
        this.isPublished = true;
    }

    public void setData(JobEventPayload data) {
        this.data = data;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount++;
        this.lastRetryAt = Instant.now().toEpochMilli();
    }


    public void markPublishedToQueue() {
        this.publishedToQueue = true;
        this.publishedToQueueAt = Instant.now().toEpochMilli();
    }
}
