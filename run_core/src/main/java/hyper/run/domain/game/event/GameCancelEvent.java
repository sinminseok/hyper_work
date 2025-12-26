package hyper.run.domain.game.event;

import hyper.run.domain.game.entity.Game;

public class GameCancelEvent {
    private final Long gameId;
    private final Long userId;

    public GameCancelEvent(Long gameId, Long userId) {
        this.gameId = gameId;
        this.userId = userId;
    }

    public static GameCancelEvent from(Game game, Long userId){
        return new GameCancelEvent(game.getId(), userId);
    }

}
