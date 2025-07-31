package hyper.run.helper;

import hyper.run.domain.game.dto.request.GameHistoryUpdateRequest;
import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameDistance;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.entity.GameType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class GameHelper {

    public static Game generateGame(GameType gameType, GameDistance gameDistance){
        return Game.builder()
                .name("테스트용경기")
                .type(gameType)
                .distance(gameDistance)
                .gameDate(LocalDate.now())
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusHours(5))
                .participatedCount(10)
                .totalPrize(1000)
                .firstPlacePrize(800)
                .secondPlacePrize(150)
                .thirdPlacePrize(50)
                .build();
    }

    public static GameHistory generateCadenceGameHistory(String id, Long gameId, Long userId, int targetCadence, boolean isDone){
        return GameHistory.builder()
                .id(id)
                .gameId(gameId)
                .userId(userId)
                .gameDistance(GameDistance.FIVE_KM_COURSE)
                .rank(0)
                .prize(0)
                .targetCadence(targetCadence)
                .done(isDone)
                .build();
    }

    public static GameHistoryUpdateRequest generateCadenceGameHistoryUpdateRequest(Long gameId, Long userId, double currentCadence){
        return GameHistoryUpdateRequest.builder()
                .gameId(gameId)
                .userId(userId)
                .currentCadence(currentCadence)
                .build();
    }
}
