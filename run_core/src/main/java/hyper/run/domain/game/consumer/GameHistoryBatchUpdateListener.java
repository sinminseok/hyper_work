package hyper.run.domain.game.consumer;

import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.event.GameHistoryBatchUpdateEvent;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.game.service.GameHistoryCacheService;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static hyper.run.exception.ErrorMessages.NOT_EXIST_GAME_HISTORY;

@Component
@RequiredArgsConstructor
public class GameHistoryBatchUpdateListener {

    private final GameHistoryRepository gameHistoryRepository;
    private final GameHistoryCacheService gameHistoryCacheService;

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

        gameHistoryCacheService.updateUserStatusCache(event.gameId(), event.userId(), gameHistory);
    }
}
