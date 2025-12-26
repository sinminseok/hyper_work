package hyper.run.domain.outbox.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOutboxEvent is a Querydsl query type for OutboxEvent
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOutboxEvent extends EntityPathBase<OutboxEvent> {

    private static final long serialVersionUID = -64946786L;

    public static final QOutboxEvent outboxEvent = new QOutboxEvent("outboxEvent");

    public final NumberPath<Long> createdAt = createNumber("createdAt", Long.class);

    public final SimplePath<hyper.run.common.job.JobEventPayload> data = createSimple("data", hyper.run.common.job.JobEventPayload.class);

    public final StringPath id = createString("id");

    public final BooleanPath isPublished = createBoolean("isPublished");

    public final NumberPath<Long> lastRetryAt = createNumber("lastRetryAt", Long.class);

    public final BooleanPath publishedToQueue = createBoolean("publishedToQueue");

    public final NumberPath<Long> publishedToQueueAt = createNumber("publishedToQueueAt", Long.class);

    public final NumberPath<Integer> retryCount = createNumber("retryCount", Integer.class);

    public final EnumPath<hyper.run.common.enums.JobType> type = createEnum("type", hyper.run.common.enums.JobType.class);

    public QOutboxEvent(String variable) {
        super(OutboxEvent.class, forVariable(variable));
    }

    public QOutboxEvent(Path<? extends OutboxEvent> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOutboxEvent(PathMetadata metadata) {
        super(OutboxEvent.class, metadata);
    }

}

