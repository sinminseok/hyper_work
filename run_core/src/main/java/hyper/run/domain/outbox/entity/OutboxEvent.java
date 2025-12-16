package hyper.run.domain.outbox.entity;

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

    private OutboxEventType type;

    private boolean isPublished;

    private long createdAt;

    @Convert(converter = OutboxEventData.OutboxEventDataConverter.class)
    private OutboxEventData data;

    public void prePersist() {
        this.createdAt = Instant.now().toEpochMilli();
    }

    public OutboxEvent(OutboxEventType type, OutboxEventData data) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.isPublished = false;
        this.data = data;
        registerEvent(new OutboxCommittedEvent(id, type, data));
    }

    public static final int PUBLISH_MINIMUM_SECONDS = 300;
    public static final int PUBLISH_MAXIMUM_SECONDS = 900;

    public String getId() {
        return id;
    }

    public OutboxEventType getType() {
        return type;
    }

    public OutboxEventData getData() {
        return data;
    }

    public boolean isPublished() {
        return this.isPublished;
    }

    public void publish() {
        this.isPublished = true;
    }

    public void setData(OutboxEventData data) {
        this.data = data;
    }
}
