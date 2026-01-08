package hyper.run.domain.game.event;

import hyper.run.common.job.JobEventPayload;
import hyper.run.common.enums.JobType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GameFinishedJobPayload extends JobEventPayload {

    private Long gameId;
    private String finishedAt;

    public GameFinishedJobPayload(Long gameId, String finishedAt) {
        this.gameId = gameId;
        this.finishedAt = finishedAt;
    }

    @Override
    public JobType getType() {
        return JobType.GAME_FINISHED;
    }

    public static GameFinishedJobPayload from(GameFinishedEvent event) {
        return new GameFinishedJobPayload(
                event.getGameId(),
                event.getFinishedAt().toString()
        );
    }
}
