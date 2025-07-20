package hyper.run.domain.game.dto.response;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameDistance;
import hyper.run.domain.game.entity.GameStatus;
import hyper.run.domain.game.entity.GameType;
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

    private LocalDate gameDate;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private int participatedCount;

    private double firstPlacePrize;

    private double secondPlacePrize;

    private double thirdPlacePrize;

    private GameStatus status;

    public static GameResponse toResponse(Game game, GameStatus status) {
        return GameResponse.builder()
                .id(game.getId())
                .name(game.getName())
                .type(game.getType())
                .gameDate(game.getGameDate())
                .distance(game.getDistance())
                .startAt(game.getStartAt())
                .endAt(game.getEndAt())
                .participatedCount(game.getParticipatedCount())
                .firstPlacePrize(game.getFirstPlacePrize())
                .secondPlacePrize(game.getSecondPlacePrize())
                .thirdPlacePrize(game.getThirdPlacePrize())
                .status(status)
                .build();
    }
}
