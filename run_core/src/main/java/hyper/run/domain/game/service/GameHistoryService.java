package hyper.run.domain.game.service;

import hyper.run.domain.game.dto.request.GameHistoryUpdateRequest;
import hyper.run.domain.game.dto.response.GameHistoryResponse;
import hyper.run.domain.game.dto.response.GameInProgressWatchResponse;
import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static hyper.run.exception.ErrorMessages.NOT_EXIST_GAME_HISTORY;
import static hyper.run.exception.ErrorMessages.NOT_EXIST_USER_ID;

@Service
@RequiredArgsConstructor
public class GameHistoryService {

    private final Map<GameType, GameRankService> gameRankServices;
    private final GameHistoryRepository repository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    @Transactional
    public GameInProgressWatchResponse updateGameHistory(final GameHistoryUpdateRequest request) {
        GameHistory gameHistory = getGameHistory(request);
        updateWatchConnection(gameHistory);
        updateGameHistoryFromRequest(gameHistory, request);
        return GameInProgressWatchResponse.toResponse(gameHistory);
    }

    @Async
    @Transactional
    public void updateAsyncGameHistory(final GameHistoryUpdateRequest request) {
        GameHistory gameHistory = getGameHistory(request);
        updateWatchConnection(gameHistory);
        updateGameHistoryFromRequest(gameHistory, request);
    }

    @Transactional
    public void quitGame(final GameHistoryUpdateRequest request) {
        GameHistory gameHistory = getGameHistory(request);
        Game game = getGameById(gameHistory.getGameId());
        calculateRank(game);
        markGameAsDone(gameHistory);
    }

    /**
     * 시작되지 않은 경기에 대해 쿠폰을 환불해주는 메서드
     */
    @Transactional
    public void stopGame(final Long gameId) {
        List<GameHistory> histories = repository.findAllByGameId(gameId);
        refundCoupons(histories);
        repository.deleteAll(histories);
    }

    private GameHistory getGameHistory(GameHistoryUpdateRequest request) {
        return OptionalUtil.getOrElseThrow(
                repository.findByUserIdAndGameId(request.getUserId(), request.getGameId()),
                NOT_EXIST_GAME_HISTORY
        );
    }

    private Game getGameById(Long gameId) {
        return OptionalUtil.getOrElseThrow(gameRepository.findById(gameId), "존재하지 않는 게임 ID 입니다.");
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

    private void calculateRank(Game game) {
        GameRankService gameRankService = gameRankServices.get(game.getType());
        gameRankService.calculateRank(game);
    }

    private void markGameAsDone(GameHistory gameHistory) {
        gameHistory.setDone(true);
        repository.save(gameHistory);
    }

    private void refundCoupons(List<GameHistory> histories) {
        for (GameHistory history : histories) {
            User user = OptionalUtil.getOrElseThrow(
                    userRepository.findById(history.getUserId()),
                    NOT_EXIST_USER_ID
            );
            user.increaseCoupon();
        }
    }
}
