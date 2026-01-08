package hyper.run.domain.game.consumer;

import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.event.GameCancelEvent;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static hyper.run.exception.ErrorMessages.NOT_EXIST_GAME_GISTORY_ID;
import static hyper.run.exception.ErrorMessages.NOT_EXIST_USER_EMAIL;

@Component
@RequiredArgsConstructor
public class GameCancelListener {

    private final UserRepository userRepository;
    private final GameHistoryRepository gameHistoryRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleGameCancel(GameCancelEvent event) {
        User user = OptionalUtil.getOrElseThrow(userRepository.findByIdForUpdate(event.userId()), NOT_EXIST_USER_EMAIL);
        user.increaseCoupon();

        GameHistory gameHistory = OptionalUtil.getOrElseThrow(gameHistoryRepository.findByUserIdAndGameId(event.userId(), event.gameId()), NOT_EXIST_GAME_GISTORY_ID);

        gameHistoryRepository.delete(gameHistory);
    }
}
