package hyper.run.domain.game.service.helper;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameDistance;
import hyper.run.domain.game.entity.GameType;

import java.time.LocalDate;

import static hyper.run.domain.game.utils.GamePrizeCalculator.*;

public class GameHelper {

    public static Game createGame(GameDistance distance, LocalDate date, int startTime, double totalPrize) {
        return Game.builder()
                .name(GameType.CADENCE.getName() + "-" + distance.getName())
                .type(GameType.CADENCE)
                .distance(distance)
                .gameDate(date)
                .startAt(date.atTime(startTime, 0))
                .endAt(date.atTime(startTime, 0).plusHours(distance.getTime()))
                .participatedCount(0)
                .totalPrize(totalPrize)
                .firstPlacePrize(calculateFirstPlacePrize(totalPrize))
                .secondPlacePrize(calculateSecondPlacePrize(totalPrize))
                .thirdPlacePrize(calculateThirdPlacePrize(totalPrize))
                .build();
    }
}
