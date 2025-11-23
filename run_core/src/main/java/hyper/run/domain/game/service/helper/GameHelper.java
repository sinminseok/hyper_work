package hyper.run.domain.game.service.helper;

import hyper.run.domain.game.entity.AdminGameStatus;
import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameDistance;
import hyper.run.domain.game.entity.GameType;

import java.time.LocalDate;

import static hyper.run.domain.game.utils.GamePrizeCalculator.*;

public class GameHelper {

    public static Game createGame(GameType gameType, GameDistance distance, LocalDate date, int startTime) {
        return Game.builder()
                .name(gameType.getName() + "-" + distance.getName())
                .type(gameType)
                .distance(distance)
                .gameDate(date)
                .startAt(date.atTime(startTime, 0))
                .endAt(date.atTime(startTime, 0).plusHours(distance.getTime()))
                .participatedCount(0)
                .totalPrize(0)
                .firstPlacePrize(calculateFirstPlacePrize(0))
                .secondPlacePrize(calculateSecondPlacePrize(0))
                .thirdPlacePrize(calculateThirdPlacePrize(0))
                .adminGameStatus(AdminGameStatus.SCHEDULED)
                .build();
    }
}
