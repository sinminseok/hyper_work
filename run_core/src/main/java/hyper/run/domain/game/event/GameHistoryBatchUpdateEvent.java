package hyper.run.domain.game.event;

import hyper.run.domain.game.dto.request.GameHistoryBatchUpdateRequest;

/**
 * Apple Watch용 HTTP Polling 최적화 - 배치 업데이트 이벤트
 * 클라이언트에서 여러 생체 데이터 샘플을 모아서 전송할 때 발행
 */
public record GameHistoryBatchUpdateEvent(
        Long gameId,
        Long userId,
        GameHistoryBatchUpdateRequest request
) {
    public static GameHistoryBatchUpdateEvent from(GameHistoryBatchUpdateRequest request) {
        return new GameHistoryBatchUpdateEvent(
                request.getGameId(),
                request.getUserId(),
                request
        );
    }
}
