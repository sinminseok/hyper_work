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

        histories.sort(
                Comparator
                        .comparing(GameHistory::isDone)
                        .reversed()
                        .thenComparingDouble(GameHistory::getHeartBeatScore)
        );

        return histories;
    }


    @Override
    public void generateGame(LocalDate date) {
        for (GameDistance distance : GameDistance.values()) {
            for (int i = 5; i <= 23; i += distance.getTime()) {
                if (i + distance.getTime() > 24) break;
                Game game = createGame(GameType.HEARTBEAT, distance, date, i);
                gameRepository.save(game);
            }
        }
    }

    @Override
    public GameType getGameType() {
        return GameType.HEARTBEAT;
    }

}