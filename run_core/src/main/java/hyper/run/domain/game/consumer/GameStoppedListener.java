package hyper.run.domain.game.consumer;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.event.GameStoppedEvent;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

import static hyper.run.exception.ErrorMessages.NOT_EXIST_GAME_ID;
import static hyper.run.exception.ErrorMessages.NOT_EXIST_USER_ID;

/**
 * 경기 시작 실패 이벤트 리스너
 * 참가자들에게 쿠폰 환불 후 경기 히스토리와 경기 삭제
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GameStoppedListener {

    private final GameRepository gameRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final UserRepository userRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleGameStopped(GameStoppedEvent event) {
        Long gameId = event.gameId();

        List<GameHistory> histories = gameHistoryRepository.findAllByGameId(gameId);
        refundCoupons(histories);
        gameHistoryRepository.deleteAll(histories);

        Game game = OptionalUtil.getOrElseThrow(gameRepository.findById(gameId), NOT_EXIST_GAME_ID);
        gameRepository.delete(game);
    }

    private void refundCoupons(List<GameHistory> histories) {
        for (GameHistory history : histories) {
            User user = OptionalUtil.getOrElseThrow(userRepository.findById(history.getUserId()), NOT_EXIST_USER_ID);
            user.increaseCoupon();
        }
    }
}
