package hyper.run.domain.game.service.scheduler;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.domain.game.service.GameHistoryService;
import hyper.run.domain.game.service.GameRankService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Service
public class GameScheduler {

    private final GameRepository gameRepository;
    private final GameHistoryService gameHistoryService;
    private final Map<GameType, GameRankService> gameRankServices;

    //todo 삭제
    public void startGameByTest(Game game){
        startGameRankLoop(game);
    }

    /**
     * 게임 시작 스케줄러
     * 매일 5시부터 23시까지 매시 정각 실행
     */
    @Scheduled(cron = "0 0 5-23 * * *")
    private void startGames() {
        LocalDateTime now = LocalDateTime.now();
        List<Game> games = gameRepository.findGamesByDateAndHour(now.toLocalDate(), now.getHour());
        for (Game game : games) {
            if(game.canNotStartGame()){
                gameHistoryService.stopGame(game.getId());
                gameRepository.delete(game);
            }else{
                startGameRankLoop(game);
            }
        }
    }

    public void startGameRankLoop(Game game) {
        Timer timer = new Timer(true);
        TimerTask task = createGameRankTask(game, timer);
        timer.scheduleAtFixedRate(task, 0, 15_000); // 15초마다 실행
    }

    private TimerTask createGameRankTask(Game game, Timer timer) {
        return new TimerTask() {
            @Override
            public void run() {
                GameRankService service = gameRankServices.get(game.getType());
                if (isBeforeGameEnd(game)) {
                    updateGameRank(service, game);
                } else {
                    finishGame(timer, service, game);
                }
            }
        };
    }

    private boolean isBeforeGameEnd(Game game) {
        return LocalDateTime.now().isBefore(game.getEndAt());
    }

    private void updateGameRank(GameRankService service, Game game) {
        if (service != null) {
            service.calculateRank(game);
        }
    }

    private void finishGame(Timer timer, GameRankService service, Game game) {
        timer.cancel();
        if (service != null) {
            service.saveGameResult(game);
        }
    }
}
