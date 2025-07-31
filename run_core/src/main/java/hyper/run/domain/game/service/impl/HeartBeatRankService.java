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

import static hyper.run.exception.ErrorMessages.NOT_EXIST_USER_ID;

/**
 * 사용자가 설정한 BPM(심박수)에 가까울수록 높은 점수
 */
@Service
public class HeartBeatRankService extends AbstractGameRankService {

    private final GameRepository gameRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final UserRepository userRepository;

    public HeartBeatRankService(GameHistoryRepository gameHistoryRepository, UserRepository userRepository, GameRepository gameRepository, GameHistoryRepository gameHistoryRepository1, UserRepository userRepository1) {
        super(gameHistoryRepository, userRepository);
        this.gameRepository = gameRepository;
        this.gameHistoryRepository = gameHistoryRepository1;
        this.userRepository = userRepository1;
    }

    @Override
    protected List<GameHistory> fetchSortedHistories(Game game) {
        List<GameHistory> histories = gameHistoryRepository.findAllByGameId(game.getId());

        histories.sort((g1, g2) -> {
            if (g1.isDone() && !g2.isDone()) return -1;
            if (!g1.isDone() && g2.isDone()) return 1;
            return Double.compare(g1.calculateHeartBeatScore(), g2.calculateHeartBeatScore());
        });

        return histories;
    }

    @Override
    public void generateGame(LocalDate date, double totalPrize) {

    }

    @Override
    public GameType getGameType() {
        return GameType.HEARTBEAT;
    }

}