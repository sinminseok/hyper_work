package hyper.run.domain.game.consumer;

import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.event.GameHistoryBatchUpdateEvent;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.game.service.GameHistoryCacheService;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static hyper.run.exception.ErrorMessages.NOT_EXIST_GAME_HISTORY;

/**
 * Apple Watch용 HTTP Polling 최적화 - 배치 업데이트 리스너
 * GameHistoryBatchUpdateEvent를 수신하여 생체 데이터를 업데이트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GameHistoryBatchUpdateListener {

    private final GameHistoryRepository gameHistoryRepository;
    private final GameHistoryCacheService firstPlaceCacheService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBatchUpdate(GameHistoryBatchUpdateEvent event) {

        GameHistory gameHistory = OptionalUtil.getOrElseThrow(gameHistoryRepository.findByUserIdAndGameId(event.userId(), event.gameId()), NOT_EXIST_GAME_HISTORY);

        if (!gameHistory.isConnectedWatch()) {
            gameHistory.connectWatch();
        }

        gameHistory.updateFromBatch(event.request());
        gameHistory.checkDoneByDistance();

        gameHistoryRepository.save(gameHistory);

        // DB 업데이트 후 캐시 갱신 (1위 + 해당 사용자)
        firstPlaceCacheService.updateFirstPlaceCache(event.gameId());
        firstPlaceCacheService.updateUserStatusCache(event.gameId(), event.userId(), gameHistory);
    }
}
