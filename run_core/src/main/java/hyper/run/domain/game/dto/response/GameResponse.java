package hyper.run.domain.game.dto.response;

import hyper.run.domain.game.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GameResponse {

    private Long id;

    private String name;

    private GameType type;

    private GameDistance distance;

    private ActivityType activityType;

    private LocalDate gameDate;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private int participatedCount;

    private double totalPrize;

    private double firstPlacePrize;

    private double secondPlacePrize;

    private double thirdPlacePrize;

    private double fourthPlacePrize;

    private double otherPlacePrize;

    private GameStatus status;

    private String firstUserName;

    private String secondUserName;

    private String thirdUserName;

    public static GameResponse toResponse(Game game, GameStatus status) {
        return GameResponse.builder()
                .id(game.getId())
                .name(game.getName())
                .type(game.getType())
                .activityType(game.getActivityType())
                .distance(game.getDistance())
                .startAt(game.getStartAt())
                .endAt(game.getEndAt())
                .participatedCount(game.getParticipatedCount())
                .totalPrize(nullSafePrize(game.getTotalPrize()))
                .firstPlacePrize(nullSafePrize(game.getFirstPlacePrize()))
                .secondPlacePrize(nullSafePrize(game.getSecondPlacePrize()))
                .thirdPlacePrize(nullSafePrize(game.getThirdPlacePrize()))
                .fourthPlacePrize(nullSafePrize(game.getFourthPlacePrize()))
                .otherPlacePrize(nullSafePrize(game.getOtherPlacePrize()))
                .status(status)
                .firstUserName(game.getFirstUserName())
                .secondUserName(game.getSecondUserName())
                .thirdUserName(game.getThirdUserName())
                .build();
    }

    private static double nullSafePrize(Double prize) {
        return prize != null ? prize : 0.0;
    }
}
