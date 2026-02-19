package hyper.run.domain.game.service.impl;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameDistance;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.domain.game.service.AbstractGameRankService;
import hyper.run.domain.game.service.GameHistoryCacheService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static hyper.run.domain.game.service.helper.GameHelper.createGame;

/**
 * 가장 멀리 달린 순서대로 순위를 정하는 게임
 */
@Service
public class SpeedRankService extends AbstractGameRankService {

    private final GameRepository gameRepository;
    private final GameHistoryRepository gameHistoryRepository;

    public SpeedRankService(ApplicationEventPublisher applicationEventPublisher,
                            GameHistoryCacheService gameHistoryCacheService,
                            GameHistoryRepository gameHistoryRepository,
                            GameRepository gameRepository) {
        super(applicationEventPublisher, gameHistoryCacheService, gameHistoryRepository, gameRepository);
        this.gameHistoryRepository = gameHistoryRepository;
        this.gameRepository = gameRepository;
    }

    protected List<GameHistory> fetchSortedHistories(Game game) {
        List<GameHistory> histories = gameHistoryRepository.findAllByGameId(game.getId());
        return sortHistories(histories);
    }

    @Override
    protected List<GameHistory> sortHistories(List<GameHistory> histories) {
        histories.sort(
                Comparator
                        .comparing(GameHistory::isDone)
                        .reversed()  // 완주자 우선
                        .thenComparingLong(GameHistory::getDurationInSeconds)  // 소요 시간 짧은 순 (완주자), 미완주자는 Long.MAX_VALUE
                        .thenComparingDouble(GameHistory::getRemainingDistance)  // 미완주자: 남은 거리 적은 순
        );
        return histories;
    }

    @Override
    public void generateGame(LocalDate date) {
        for (GameDistance distance : GameDistance.values()) {
            if(distance.isWalk()) continue; // 걷기 유형은 x
            if(distance == GameDistance.NEWBIE_COURSE) continue; // 왕초보 경기는 스피드 제외
            for (int hour = 5; hour <= 23; hour++) {
                Game game = createGame(GameType.SPEED, distance, date, hour, 0);
                gameRepository.save(game);
            }
        }
    }

    @Override
    public GameType getGameType() {
        return GameType.SPEED;
    }
}
