package hyper.run.domain.game.service;

import hyper.run.domain.game.dto.request.GameHistoryBatchUpdateRequest;
import hyper.run.domain.game.dto.request.GameHistoryUpdateRequest;
import hyper.run.domain.game.dto.request.GameStartRequest;
import hyper.run.domain.game.event.GameHistoryBatchUpdateEvent;
import hyper.run.domain.game.entity.ConnectType;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.user.entity.UserWatch;
import hyper.run.domain.user.repository.UserWatchRepository;
import hyper.run.exception.custom.NotFoundDataException;
import hyper.run.exception.custom.WatchMismatchException;
import hyper.run.exception.custom.WatchNotSelectedException;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static hyper.run.exception.ErrorMessages.*;

@Service
@RequiredArgsConstructor
public class GameHistoryService {

    private final GameHistoryRepository repository;
    private final UserWatchRepository userWatchRepository;
    private final ApplicationEventPublisher eventPublisher;


    @Async
    @Transactional
    public void updateAsyncGameHistory(final GameHistoryUpdateRequest request) {
        GameHistory gameHistory = getGameHistory(request);
        validateWatch(gameHistory, request.getWatchId());
        updateWatchConnection(gameHistory);
        updateGameHistoryFromRequest(gameHistory, request);
    }


    @Transactional
    public void updateBatchGameHistory(final GameHistoryBatchUpdateRequest request) {
        eventPublisher.publishEvent(GameHistoryBatchUpdateEvent.from(request));
    }

    /**
     * 경기 참가 시작 - ConnectType 등록 및 실제 시작 시간 기록
     * WATCH 모드일 경우 watchId를 지정하여 경기에 사용할 워치를 선택
     */
    @Transactional
    public void startGame(final Long userId, final GameStartRequest request) {
        GameHistory gameHistory = OptionalUtil.getOrElseThrow(
                repository.findByUserIdAndGameId(userId, request.getGameId()),
                NOT_EXIST_GAME_HISTORY
        );
        gameHistory.setConnectType(request.getConnectType());
        gameHistory.setStartAt(java.time.LocalDateTime.now());

        if (request.getConnectType() == ConnectType.WATCH) {
            Long resolvedWatchId = resolveWatchId(userId, request.getWatchId());
            gameHistory.setWatchId(resolvedWatchId);
        }

        repository.save(gameHistory);
    }

    /**
     * watchId 결정 로직:
     * - watchId가 명시된 경우: 해당 워치 소유 검증 후 반환
     * - watchId가 null이고 워치 1개: 자동 선택
     * - watchId가 null이고 워치 2개 이상: WatchNotSelectedException
     */
    private Long resolveWatchId(Long userId, Long watchId) {
        if (watchId != null) {
            userWatchRepository.findByIdAndUserId(watchId, userId)
                    .orElseThrow(() -> new NotFoundDataException(NOT_EXIST_USER_WATCH_ID));
            return watchId;
        }

        List<UserWatch> watches = userWatchRepository.findAllByUserId(userId);
        if (watches.size() == 1) {
            return watches.get(0).getId();
        }
        throw new WatchNotSelectedException(WATCH_NOT_SELECTED);
    }

    /**
     * 경기에 지정된 워치와 요청의 워치 일치 여부 검증
     * gameHistory.watchId가 null이면 검증 스킵 (기존 경기 하위호환)
     */
    private void validateWatch(GameHistory gameHistory, Long requestWatchId) {
        if (gameHistory.getWatchId() == null) {
            return;
        }
        if (requestWatchId == null || !gameHistory.getWatchId().equals(requestWatchId)) {
            throw new WatchMismatchException(WATCH_MISMATCH);
        }
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
