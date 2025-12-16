package hyper.run.domain.outbox.application;

import hyper.run.domain.outbox.entity.OutboxEventData;
import hyper.run.domain.outbox.entity.OutboxEventType;
import hyper.run.domain.sns.OutboxEventSnsMessage;

public class AppleInAppProcessor extends OutboxEventPublishProcessor {

    //private final SnsToJobRunnerMessagingService snsToJobRunnerMessagingService;

    @Override
    protected void publish(String eventId, OutboxEventData data) {
        OutboxEventSnsMessage message = new OutboxEventSnsMessage(eventId, data);
//        snsToJobRunnerMessagingService.sendMessage(
//                message, JobGroup.POST, PostJobName.SYNC_POST_CHANNEL_SETTING, JobType.ASYNC, eventId, eventId
//        );
    }

    @Override
    protected OutboxEventType getType() {
        return OutboxEventType.APPLE_IN_APP;
    }
}
