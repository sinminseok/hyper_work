package hyper.run.domain.game.dto.response;

import hyper.run.domain.game.entity.GameHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * WearOS 에 응답할 현재 게임 진행 상태 DTO
 */
@Getter
@Builder
public class GameInProgressWatchResponse {

    private int rank;

    private Integer targetBpm;

    private Integer targetCadence;

    private double currentBpm;

    private double currentCadence;

    private double currentDistance;

    private double currentFlightTime;

    private double currentGroundContactTime;

    private double currentPower;

    private double currentVerticalOscillation;

    private double currentSpeed;

    private boolean isDone;

    private boolean connectedWatch;

    public static GameInProgressWatchResponse toResponse(GameHistory gameHistory){
        return GameInProgressWatchResponse.builder()
                .rank(gameHistory.getRank())
                .targetBpm(gameHistory.getTargetBpm())
                .currentBpm(gameHistory.getCurrentBpm())
                .currentCadence(gameHistory.getCurrentCadence())
                .currentDistance(gameHistory.getCurrentDistance())
                .currentFlightTime(gameHistory.getCurrentFlightTime())
                .currentGroundContactTime(gameHistory.getCurrentGroundContactTime())
                .currentPower(gameHistory.getCurrentPower())
                .currentVerticalOscillation(gameHistory.getCurrentVerticalOscillation())
                .currentSpeed(gameHistory.getCurrentSpeed())
                .isDone(gameHistory.isDone())
                .connectedWatch(gameHistory.isConnectedWatch())
                .build();
    }
}
