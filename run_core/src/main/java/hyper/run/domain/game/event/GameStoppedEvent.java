package hyper.run.domain.game.event;

/**
 * 경기 시작 실패 이벤트
 * 참가자 부족 등의 이유로 경기가 시작되지 못하고 취소될 때 발행
 */
public record GameStoppedEvent(
        Long gameId
) {
    public static GameStoppedEvent from(Long gameId) {
        return new GameStoppedEvent(gameId);
    }
}
