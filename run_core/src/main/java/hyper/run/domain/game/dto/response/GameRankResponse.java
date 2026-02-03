package hyper.run.domain.game.dto.response;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameDistance;
import hyper.run.domain.game.entity.GameType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRankResponse {
    private Long gameId;

    private String name;

    private GameType type;

    private GameDistance distance;

    private LocalDateTime startAt;

    private double totalPrize;

    public static GameRankResponse toResponse(Game game) {
        return GameRankResponse.builder()
                .gameId(game.getId())
                .name(game.getName())
                .type(game.getType())
                .distance(game.getDistance())
                .startAt(game.getStartAt())
                .totalPrize(game.getTotalPrize())
                .build();
    }
}
