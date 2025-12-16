package hyper.run.domain.outbox.data;

import hyper.run.domain.outbox.entity.OutboxEventData;
import hyper.run.domain.outbox.entity.OutboxEventType;

public class AppleInAppData extends OutboxEventData {

    private Long postId;

    public AppleInAppData(Long postId) {
        this.postId = postId;
    }

    @Override
    public OutboxEventType getType() {
        return OutboxEventType.APPLE_IN_APP;
    }
}