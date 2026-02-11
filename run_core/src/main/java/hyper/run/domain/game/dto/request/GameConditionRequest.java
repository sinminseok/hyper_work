package hyper.run.domain.game.dto.request;

import hyper.run.domain.game.entity.ActivityType;
import hyper.run.domain.game.entity.GameDistance;
import hyper.run.domain.game.entity.GameType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GameConditionRequest {

    private LocalDateTime startAt;

    private GameDistance distance;

    private GameType type;

    private ActivityType activityType;
}
