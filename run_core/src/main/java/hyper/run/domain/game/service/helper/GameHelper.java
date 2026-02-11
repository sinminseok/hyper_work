package hyper.run.domain.game.service.helper;

import hyper.run.domain.game.entity.*;

import java.time.LocalDate;

import static hyper.run.domain.game.utils.GamePrizeCalculator.*;

public class GameHelper {

    public static Game createGame(GameType gameType, GameDistance distance, LocalDate date, int startHour, int startMinute) {
        ActivityType activityType = distance.isWalk() ? ActivityType.WALKING : ActivityType.RUNNING;

        return Game.builder()
                .name(gameType.getName() + "-" + distance.getName())
                .type(gameType)
                .activityType(activityType)
                .distance(distance)
                .startAt(date.atTime(startHour, startMinute))
                .endAt(date.atTime(startHour, startMinute).plusMinutes(distance.getTime()))
                .participatedCount(0)
                .totalPrize(0.0)
                .firstPlacePrize(calculateFirstPlacePrize(0.0))
                .secondPlacePrize(calculateSecondPlacePrize(0.0))
                .thirdPlacePrize(calculateThirdPlacePrize(0.0))
                .fourthPlacePrize(calculateFourthPlacePrize(0.0))
                .otherPlacePrize(calculateOtherPlacePrize(0.0))
                .adminGameStatus(AdminGameStatus.SCHEDULED)
                .build();
    }
}
