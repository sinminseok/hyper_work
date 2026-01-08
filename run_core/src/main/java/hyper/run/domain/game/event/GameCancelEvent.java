package hyper.run.domain.game.event;

public record GameCancelEvent(
        Long userId,
        Long gameId
) {
    public static GameCancelEvent from(Long userId, Long gameId) {
        return new GameCancelEvent(userId, gameId);
    }
}
