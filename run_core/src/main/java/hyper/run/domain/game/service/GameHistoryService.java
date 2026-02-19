package hyper.run.domain.game.service;

import hyper.run.domain.game.dto.request.GameHistoryBatchUpdateRequest;
import hyper.run.domain.game.dto.request.GameHistoryUpdateRequest;
import hyper.run.domain.game.dto.request.GameStartRequest;
import hyper.run.domain.game.event.GameHistoryBatchUpdateEvent;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import static hyper.run.exception.ErrorMessages.NOT_EXIST_GAME_HISTORY;

@Service
@RequiredArgsConstructor
public class GameHistoryService {

    private final GameHistoryRepository repository;
    private final ApplicationEventPublisher eventPublisher;


    @Async
    @Transactional
    public void updateAsyncGameHistory(final GameHistoryUpdateRequest request) {
        GameHistory gameHistory = getGameHistory(request);
        updateWatchConnection(gameHistory);
        updateGameHistoryFromRequest(gameHistory, request);
    }


    @Transactional
    public void updateBatchGameHistory(final GameHistoryBatchUpdateRequest request) {
        eventPublisher.publishEvent(GameHistoryBatchUpdateEvent.from(request));
    }

    /**
     * 경기 참가 시작 - ConnectType 등록 및 실제 시작 시간 기록
     */
    @Transactional
    public void startGame(final Long userId, final GameStartRequest request) {
        GameHistory gameHistory = OptionalUtil.getOrElseThrow(
                repository.findByUserIdAndGameId(userId, request.getGameId()),
                NOT_EXIST_GAME_HISTORY
        );
        gameHistory.setConnectType(request.getConnectType());
        gameHistory.setStartAt(java.time.LocalDateTime.now());
        repository.save(gameHistory);
    }


    private GameHistory getGameHistory(GameHistoryUpdateRequest request) {
        return OptionalUtil.getOrElseThrow(
                repository.findByUserIdAndGameId(request.getUserId(), request.getGameId()),
                NOT_EXIST_GAME_HISTORY
        );
    }

    private void updateWatchConnection(GameHistory gameHistory) {
        if (!gameHistory.isConnectedWatch()) {
            gameHistory.connectWatch();
        }
    }

    private void updateGameHistoryFromRequest(GameHistory gameHistory, GameHistoryUpdateRequest request) {
        gameHistory.updateFrom(request);
        gameHistory.checkDoneByDistance();
        repository.save(gameHistory);
    }

}
