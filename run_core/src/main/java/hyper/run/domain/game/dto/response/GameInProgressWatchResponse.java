package hyper.run.domain.game.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import hyper.run.domain.game.entity.GameHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * WearOS 에 응답할 현재 게임 진행 상태 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameInProgressWatchResponse {

    private int rank;

    private Integer targetBpm;

    private Integer targetCadence;

    private double currentBpm;

    private double currentCadence;

    private double currentDistance;

    private double targetDistance;


    @JsonProperty("done")
    private boolean isDone;

    private boolean connectedWatch;

    private int pollInterval;

    public static GameInProgressWatchResponse toResponse(GameHistory gameHistory){
        double targetDist = gameHistory.getGameDistance().getDistance();
        int pollInterval = calculatePollInterval(gameHistory.getCurrentDistance(), targetDist, gameHistory.isDone());

        return GameInProgressWatchResponse.builder()
                .rank(gameHistory.getRank())
                .targetBpm(gameHistory.getTargetBpm())
                .currentBpm(gameHistory.getCurrentBpm())
                .currentCadence(gameHistory.getCurrentCadence())
                .currentDistance(gameHistory.getCurrentDistance())
                .targetDistance(targetDist)
                .isDone(gameHistory.isDone())
                .connectedWatch(gameHistory.isConnectedWatch())
                .pollInterval(pollInterval)
                .build();
    }

    private static int calculatePollInterval(double currentDistance, double targetDistance, boolean isDone) {
        if (isDone) {
            return -1;
        }

        double progressRatio = currentDistance / targetDistance;

        if (progressRatio >= 0.9) {
            return 3;   // 막판 스퍼트
        } else if (progressRatio <= 0.3) {
            return 7;   // 초반
        } else {
            return 5;   // 일반
        }
    }
}
