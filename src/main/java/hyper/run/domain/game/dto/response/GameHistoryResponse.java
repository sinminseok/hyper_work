package hyper.run.domain.game.dto.response;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameDistance;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class GameHistoryResponse {

    private LocalDate gameDate;

    private Long gameId;

    private GameType gameType;

    private GameDistance gameDistance;

    private LocalDateTime endAt;

    private int myRank;

    private int participatedCount;

    private double myPrize;

    public static GameHistoryResponse toResponse(Game game , GameHistory gameHistory){
        return GameHistoryResponse.builder()
                .gameDate(game.getGameDate())
                .gameId(game.getId())
                .gameType(game.getType())
                .gameDistance(game.getDistance())
                .endAt(game.getEndAt())
                .myRank(gameHistory.getRank())
                .myPrize(gameHistory.getPrize())
                .build();
    }
}
