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

/**
 * 가장 멀리 달린 순서대로 순위를 정하는 게임
 */
@Service
public class SpeedRankService extends AbstractGameRankService {

    private final GameRepository gameRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final UserRepository userRepository;

    public SpeedRankService(GameRepository gameRepository, GameHistoryRepository gameHistoryRepository, UserRepository userRepository, GameHistoryRepository gameHistoryRepository1, UserRepository userRepository1) {
        super(gameHistoryRepository, userRepository);
        this.gameHistoryRepository = gameHistoryRepository1;
        this.userRepository = userRepository1;
        this.gameRepository = gameRepository;
    }


    protected List<GameHistory> fetchSortedHistories(Game game) {
        List<GameHistory> histories = gameHistoryRepository.findAllByGameId(game.getId());
        histories.sort(
                Comparator
                        .comparing(GameHistory::isDone)
                        .reversed()
                        .thenComparingDouble(GameHistory::getRemainingDistance)
        );

        return histories;
    }

    @Override
    public void generateGame(LocalDate date) {
        for (GameDistance distance : GameDistance.values()) {
            for (int i = 5; i <= 23; i += distance.getTime()) {
                if (i + distance.getTime() > 24) break;
                Game game = createGame(GameType.SPEED, distance, date, i);
                gameRepository.save(game);
            }
        }
    }

    @Override
    public GameType getGameType() {
        return GameType.SPEED;
    }
}
