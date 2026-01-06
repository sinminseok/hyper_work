package hyper.run.domain.game.dto.response;

import hyper.run.domain.game.entity.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
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
                .totalPrize(game.getTotalPrize())
                .firstPlacePrize(game.getFirstPlacePrize())
                .secondPlacePrize(game.getSecondPlacePrize())
                .thirdPlacePrize(game.getThirdPlacePrize())
                .status(status)
                .firstUserName(game.getFirstUserName())
                .secondUserName(game.getSecondUserName())
                .thirdUserName(game.getThirdUserName())
                .build();
    }
}
