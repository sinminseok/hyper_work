package hyper.run.domain.game.service.impl;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameDistance;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.domain.game.service.AbstractGameRankService;
import hyper.run.domain.game.service.GameRankService;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static hyper.run.domain.game.service.helper.GameHelper.createGame;
import static hyper.run.exception.ErrorMessages.NOT_EXIST_USER_ID;

@Service
public class PowerRankService extends AbstractGameRankService {

    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final GameHistoryRepository gameHistoryRepository;

    public PowerRankService(GameRepository gameRepository, GameHistoryRepository gameHistoryRepository, UserRepository userRepository, UserRepository userRepository1, GameHistoryRepository gameHistoryRepository1) {
        super(gameHistoryRepository, userRepository);
        this.gameRepository = gameRepository;
        this.userRepository = userRepository1;
        this.gameHistoryRepository = gameHistoryRepository1;
    }

    @Override
    protected List<GameHistory> fetchSortedHistories(Game game) {
        List<GameHistory> histories = gameHistoryRepository.findAllByGameId(game.getId());

        histories.sort(
                Comparator
                        .comparing(GameHistory::isDone)
                        .reversed()
                        .thenComparing(Comparator.comparingDouble(GameHistory::getCurrentPower).reversed())
        );

        return histories;
    }

    @Override
    public void generateGame(LocalDate date) {
        for (GameDistance distance : GameDistance.values()) {
            for (int i = 5; i <= 23; i += distance.getTime()) {
                if (i + distance.getTime() > 24) break;
                Game game = createGame(GameType.POWER, distance, date, i);
                gameRepository.save(game);
            }
        }
    }

    @Override
    public GameType getGameType() {
        return GameType.POWER;
    }
}
