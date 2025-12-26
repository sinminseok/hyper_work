package hyper.run.domain.game.producer;

import hyper.run.domain.game.event.GameFinishedJobPayload;
import hyper.run.domain.outbox.application.OutboxEventPublishProcessor;
import hyper.run.common.enums.JobType;
import hyper.run.common.job.JobEventPayload;
import hyper.run.domain.game.event.GameFinishedMessage;
import hyper.run.utils.SnsPublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameFinishedProcessor extends OutboxEventPublishProcessor {

    private final SnsPublisherService snsPublisherService;

    @Override
    protected void publish(String eventId, JobEventPayload data) {
        GameFinishedJobPayload gameFinishedData = (GameFinishedJobPayload) data;
        GameFinishedMessage message = GameFinishedMessage.from(eventId, gameFinishedData);
        snsPublisherService.publish(JobType.GAME_FINISHED, message, eventId);
    }

    @Override
    protected JobType getType() {
        return JobType.GAME_FINISHED;
    }
}
