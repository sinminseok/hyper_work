package hyper.run.domain.game.service.impl;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameDistance;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.domain.game.service.AbstractGameRankService;
import hyper.run.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static hyper.run.domain.game.service.helper.GameHelper.createGame;

@Service
public class CadenceRankService extends AbstractGameRankService {

    private final GameRepository gameRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final UserRepository userRepository;


    public CadenceRankService(GameRepository gameRepository, GameHistoryRepository gameHistoryRepository, UserRepository userRepository) {
        super(gameHistoryRepository, userRepository);
        this.gameRepository = gameRepository;
        this.gameHistoryRepository = gameHistoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected List<GameHistory> fetchSortedHistories(Game game) {
        List<GameHistory> histories = gameHistoryRepository.findAllByGameId(game.getId());
        histories.sort((g1, g2) -> {
            if (g1.isDone() && !g2.isDone()) return -1;
            if (!g1.isDone() && g2.isDone()) return 1;
            return Double.compare(g1.calculateCadenceScore(), g2.calculateCadenceScore());
        });
        return histories;
    }

    @Override
    public GameType getGameType() {
        return GameType.FLIGHT_TIME;
    }

    @Override
    public void generateGame(LocalDate date) {
        for (GameDistance distance : GameDistance.values()) {
            for (int i = 5; i <= 23; i += distance.getTime()) {
                if (i + distance.getTime() > 24) break;
                Game game = createGame(GameType.CADENCE, distance, date, i);
                gameRepository.save(game);
            }
        }
    }
}
