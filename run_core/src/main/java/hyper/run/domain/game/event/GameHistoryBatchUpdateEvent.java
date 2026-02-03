package hyper.run.domain.game.event;

import hyper.run.domain.game.dto.request.GameHistoryBatchUpdateRequest;


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
