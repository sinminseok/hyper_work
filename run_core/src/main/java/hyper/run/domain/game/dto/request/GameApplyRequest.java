package hyper.run.domain.game.dto.request;

import hyper.run.domain.game.entity.ActivityType;
import hyper.run.domain.game.entity.GameDistance;
import hyper.run.domain.game.entity.GameType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GameApplyRequest {

    private LocalDateTime startAt;

    private GameDistance distance;

    private GameType type;

    private ActivityType activityType;

    private Integer averageBpm;

    private Integer targetCadence;

    private boolean isEmptyAverageBpm(){
        return this.averageBpm != null;
    }

    private boolean isEmptyAverageCadence(){
        return this.targetCadence != null;
    }
}
