package hyper.run.domain.game.event;

public class GameCancelEvent {
    private final Long gameId;

    public GameCancelEvent(Long gameId) {
        this.gameId = gameId;
    }

}
