package hyper.run.domain.game.listener;

import hyper.run.domain.game.event.GameExpiredCleanupEvent;
import hyper.run.domain.game.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameExpiredCleanupListener {

    private final GameRepository gameRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleGameExpiredCleanup(GameExpiredCleanupEvent event) {
        gameRepository.deleteAllByIdInBatch(event.gameIds());
    }
}
