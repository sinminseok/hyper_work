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


import static hyper.run.exception.ErrorMessages.NOT_EXIST_GAME_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameStoppedListener {

    private final GameRepository gameRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleGameStopped(GameStoppedEvent event) {
        //경기 종료 조건이 변경되면 여기서 작업
        Long gameId = event.gameId();
        Game game = OptionalUtil.getOrElseThrow(gameRepository.findById(gameId), NOT_EXIST_GAME_ID);
        gameRepository.delete(game);
    }
}
