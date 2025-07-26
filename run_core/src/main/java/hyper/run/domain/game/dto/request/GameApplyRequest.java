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
                .updateCount(0)
                .done(false)
                .rank(0);
        if (isEmptyAverageBpm()) {
            builder.targetBpm(this.averageBpm);
        }
        if (isEmptyAverageCadence()) {
            builder.targetCadence(this.targetCadence);
        }
        return builder.build();
    }

    private boolean isEmptyAverageBpm(){
        return this.averageBpm != null;
    }

    private boolean isEmptyAverageCadence(){
        return this.targetCadence != null;
    }
}
