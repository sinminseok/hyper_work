package hyper.run.domain.game.dto.response;

import hyper.run.domain.game.entity.ConnectType;
import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameDistance;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.entity.GameType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameHistoryResponse {

    private LocalDate gameDate;

    private Long gameId;

    private GameType gameType;

    private GameDistance gameDistance;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private int myRank;

    private int participatedCount;

    private double targetBpm;

    private double targetCadence;

    private double currentBpm;

    private double currentCadence;

    private double currentDistance;

    private double myPrize;

    private boolean connectedWatch;

    private ConnectType connectType;

    private boolean isDone;

    public static GameHistoryResponse toResponse(Game game , GameHistory gameHistory){
        return GameHistoryResponse.builder()
                .gameId(game.getId())
                .gameDate(game.getStartAt().toLocalDate())
                .gameType(game.getType())
                .gameDistance(game.getDistance())
                .endAt(game.getEndAt())
                .targetBpm(gameHistory.getTargetBpm())
                .targetCadence(gameHistory.getTargetCadence())
                .currentBpm(gameHistory.getCurrentBpm())
                .currentDistance(gameHistory.getCurrentDistance())
                .currentCadence(gameHistory.getCurrentCadence())
                .myRank(gameHistory.getRank())
                .isDone(gameHistory.isDone())
                .myPrize(gameHistory.getPrize())
                .participatedCount(game.getParticipatedCount())
                .connectedWatch(gameHistory.isConnectedWatch())
                .connectType(gameHistory.getConnectType())
                .startAt(game.getStartAt())
                .build();
    }
}
