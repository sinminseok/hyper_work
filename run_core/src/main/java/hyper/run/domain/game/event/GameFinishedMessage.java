package hyper.run.domain.game.event;

import hyper.run.common.enums.JobType;
import hyper.run.common.message.SqsMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * SQS로 전송될 게임 종료 메시지
 * GameFinishedData에서 필요한 필드만 추출하여 전송합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameFinishedMessage implements SqsMessage {

    private String outboxEventId;

    private Long gameId;

    private String finishedAt;


    public static GameFinishedMessage from(String outboxEventId, GameFinishedJobPayload data) {
        return GameFinishedMessage.builder()
                .outboxEventId(outboxEventId)
                .gameId(data.getGameId())
                .finishedAt(data.getFinishedAt())
                .build();
    }

    @Override
    public JobType getJobType() {
        return JobType.GAME_FINISHED;
    }
}

