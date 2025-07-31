package hyper.run.domain.game.service.scheduler;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.domain.game.service.GameRankService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Service
public class GameScheduler {

    private final GameRepository gameRepository;
    private final Map<GameType, GameRankService> gameRankServices;
    private final Map<Long, Timer> runningTimers = new ConcurrentHashMap<>();


    @Scheduled(cron = "0 0 5-23 * * *") // 매일 5시부터 23시까지 매시 정각 실행
    public void startGames() {
        LocalDateTime now = LocalDateTime.now();
        List<Game> games = gameRepository.findGamesByDateAndHour(now.toLocalDate(), now.getHour());
        for (Game game : games) {
            if (!runningTimers.containsKey(game.getId())) {
                startGameRankLoop(game);
            }
        }
    }

    //todo 삭제
    public void startGameByTest(Game game){
        startGameRankLoop(game);
    }

    private void startGameRankLoop(Game game) {
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                GameRankService service = gameRankServices.get(game.getType());
                if (LocalDateTime.now().isBefore(game.getEndAt())) {
                    if (service != null) {
                        service.calculateRank(game);
                    }
                } else {
                    cancel();
                    service.saveGameResult(game);
                    runningTimers.remove(game.getId());
                }
            }
        };

        //todo 30초로 변경
        timer.scheduleAtFixedRate(task, 0, 10 * 1000); // 30초마다 실행
        runningTimers.put(game.getId(), timer);
    }
}
