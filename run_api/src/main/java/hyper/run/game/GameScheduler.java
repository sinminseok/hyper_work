package hyper.run.game;

import hyper.run.domain.game.dto.response.GameInProgressWatchResponse;
import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.domain.game.service.GameHistoryService;
import hyper.run.domain.game.service.GameRankService;
import hyper.run.domain.game.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;
    private final GameService gameService;


    public void testStart(final Long gameId){
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경기입니다."));
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
            service.calculateRank(game);
            broadcastFirstPlace(game.getId());
        }
    }

    private void broadcastFirstPlace(Long gameId) {
        try {
            GameInProgressWatchResponse firstPlace = gameService.findFirstPlaceByGameId(gameId);
            messagingTemplate.convertAndSend("/sub/game/first-place/" + gameId, firstPlace);
        } catch (Exception e) {
            // 1위가 없는 경우 등의 예외 처리 (로그만 남기고 계속 진행)
            System.err.println("Failed to broadcast first place for game " + gameId + ": " + e.getMessage());
        }
    }

    private void finishGame(Timer timer, GameRankService service, Game game) {
        timer.cancel();
        if (service != null) {
            service.saveGameResult(game);
        }
    }
}
