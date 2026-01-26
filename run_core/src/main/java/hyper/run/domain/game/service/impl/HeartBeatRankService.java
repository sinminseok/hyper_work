package hyper.run.domain.game.service.impl;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameDistance;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.domain.game.service.AbstractGameRankService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static hyper.run.domain.game.service.helper.GameHelper.createGame;

/**
 * 사용자가 설정한 BPM(심박수)에 가까울수록 높은 점수
 */
@Service
public class HeartBeatRankService extends AbstractGameRankService {

    private final GameRepository gameRepository;
    private final GameHistoryRepository gameHistoryRepository;

    public HeartBeatRankService(ApplicationEventPublisher applicationEventPublisher,
                                GameHistoryRepository gameHistoryRepository,
                                GameRepository gameRepository) {
        super(applicationEventPublisher, gameHistoryRepository, gameRepository);
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
                        .thenComparingDouble(GameHistory::getHeartBeatScore)  // 심박수 점수 작은 순
                        .thenComparingLong(GameHistory::getDurationInSeconds)  // 동점 시 소요 시간 짧은 순
        );
        return histories;
    }


    @Override
    public void generateGame(LocalDate date) {
        for (GameDistance distance : GameDistance.values()) {
            for (int hour = 5; hour <= 23; hour++) {
                Game game = createGame(GameType.HEARTBEAT, distance, date, hour, 0);
                gameRepository.save(game);
            }
        }
    }

    @Override
    public GameType getGameType() {
        return GameType.HEARTBEAT;
    }

}