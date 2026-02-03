package hyper.run.game;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.game.event.GameStoppedEvent;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.domain.game.service.GameHistoryCacheService;
import hyper.run.domain.game.service.GameRankService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Service
public class GameScheduler {

    private final GameRepository gameRepository;
    private final Map<GameType, GameRankService> gameRankServices;
    private final GameHistoryCacheService gameHistoryCacheService;
    private final ApplicationEventPublisher eventPublisher;


    public void testStart(final Long gameId){
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경기입니다."));
        startGameRankLoop(game);
    }

    /**
     * 게임 시작 스케줄러
     * 매일 5시부터 23시까지 매시 정각 실행
     */
    @Transactional
    @Scheduled(cron = "0 0 5-23 * * *")
    public void startGames() {
        LocalDateTime now = LocalDateTime.now();
        List<Game> games = gameRepository.findGamesByDateAndHour(now.toLocalDate(), now.getHour());
        for (Game game : games) {
            if(game.canNotStartGame()){
                eventPublisher.publishEvent(GameStoppedEvent.from(game.getId()));
            }else{
                startGameRankLoop(game);
            }
        }
    }

    public void startGameRankLoop(Game game) {
        Timer timer = new Timer(true);
        TimerTask task = createGameRankTask(game, timer);
        timer.scheduleAtFixedRate(task, 0, 15_000); // 15초마다 순위 갱신
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
            service.calculateRank(game);  // 내부에서 Redis 캐시도 함께 업데이트
        }
    }

    private void finishGame(Timer timer, GameRankService service, Game game) {
        timer.cancel();
        gameHistoryCacheService.evictCache(game.getId());
        if (service != null) {
            service.saveGameResult(game);
        }
    }
}
