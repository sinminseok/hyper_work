package hyper.run.domain.game.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameHistoryUpdateRequest {

//    private String id;

    private Long gameId;

    private Long userId;

    private double currentBpm;

    private double currentCadence;

    private double currentDistance;

    private double currentFlightTime;

    private double currentGroundContactTime;

    private double currentPower;

    private double currentVerticalOscillation;

    private double currentSpeed;
}
