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
 * 사용자가 설정한 케이던스와 가까울 수록 이기는 경기
 */
@Service
public class CadenceRankService extends AbstractGameRankService {

    private final GameRepository gameRepository;
    private final GameHistoryRepository gameHistoryRepository;

    public CadenceRankService(ApplicationEventPublisher applicationEventPublisher,
                              GameHistoryCacheService gameHistoryCacheService,
                              GameHistoryRepository gameHistoryRepository,
                              GameRepository gameRepository) {
        super(applicationEventPublisher, gameHistoryCacheService, gameHistoryRepository, gameRepository);
        this.gameHistoryRepository = gameHistoryRepository;
        this.gameRepository = gameRepository;
    }

    @Override
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
                        .thenComparingDouble(GameHistory::getCadenceScore)  // 케이던스 점수 작은 순
                        .thenComparingLong(GameHistory::getDurationInSeconds)  // 동점 시 소요 시간 짧은 순
        );
        return histories;
    }

    @Override
    public GameType getGameType() {
        return GameType.CADENCE;
    }

    @Override
    public void generateGame(LocalDate date) {
        for (GameDistance distance : GameDistance.values()) {
            for (int hour = 5; hour <= 23; hour++) {
                Game game = createGame(GameType.CADENCE, distance, date, hour, 0);
                gameRepository.save(game);
            }
        }
    }
}
