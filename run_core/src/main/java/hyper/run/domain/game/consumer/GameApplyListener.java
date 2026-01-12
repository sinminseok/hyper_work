package hyper.run.domain.game.consumer;

import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.event.GameApplyEvent;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static hyper.run.exception.ErrorMessages.NOT_EXIST_USER_EMAIL;

@Component
@RequiredArgsConstructor
public class GameApplyListener {

    private final UserRepository userRepository;
    private final GameHistoryRepository gameHistoryRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleGameApply(GameApplyEvent event) {
        User user = OptionalUtil.getOrElseThrow(userRepository.findByIdForUpdate(event.userId()), NOT_EXIST_USER_EMAIL);
        user.validateCouponAmount();
        user.decreaseCoupon();

        gameHistoryRepository.save(GameHistory.createForApply(
                event.gameId(),
                event.userId(),
                event.gameDistance(),
                event.averageBpm(),
                event.targetCadence(),
                event.startAt(),
                event.endAt()
        ));
    }
}
