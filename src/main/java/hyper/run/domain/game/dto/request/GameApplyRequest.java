package hyper.run.domain.game.dto.request;

import hyper.run.domain.game.entity.GameHistory;
import lombok.Getter;

@Getter
public class GameApplyRequest {

    private Long gameId;

    private Integer averageBpm;

    private Integer targetCadence;

    public GameHistory toGameHistory(final Long userId){
        GameHistory.GameHistoryBuilder builder = GameHistory.builder()
                .gameId(this.gameId)
                .userId(userId)
                .prize(0)
                .rank(0);
        if (this.averageBpm != null) {
            builder.targetBpm(this.averageBpm);
        }
        if (this.targetCadence != null) {
            builder.targetCadence(this.targetCadence);
        }
        return builder.build();
    }
}
