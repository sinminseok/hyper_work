package hyper.run.domain.game.event;

import hyper.run.domain.game.entity.Game;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
public class GameFinishedEvent {

    private final Long gameId;
    private final LocalDateTime finishedAt;

    public GameFinishedEvent(Long gameId, LocalDateTime finishedAt) {
        this.gameId = gameId;
        this.finishedAt = finishedAt;
    }

    public static GameFinishedEvent from(Game game) {
        return new GameFinishedEvent(game.getId(), LocalDateTime.now());
    }
}
