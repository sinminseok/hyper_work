package hyper.run.domain.game.service.impl;

import hyper.run.domain.game.entity.Game;
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
 * 가장 멀리 달린 순서대로 순위를 정하는 게임
 * todo 단순 거리로만 랭킹을 판별하는건 반례가 있음 이를 해결해야됨 ex) 최종적으로 모든 사람이 같은 거리에 도달
 */
@Service
public class SpeedRankService extends AbstractGameRankService {

    private final GameHistoryRepository gameHistoryRepository;
    private final UserRepository userRepository;

    public SpeedRankService(GameHistoryRepository gameHistoryRepository, UserRepository userRepository, GameHistoryRepository gameHistoryRepository1, UserRepository userRepository1) {
        super(gameHistoryRepository, userRepository);
        this.gameHistoryRepository = gameHistoryRepository1;
        this.userRepository = userRepository1;
    }

    protected List<GameHistory> fetchSortedHistories(Game game) {
        List<GameHistory> histories = gameHistoryRepository.findAllByGameId(game.getId());

        histories.sort((g1, g2) -> {
            if (g1.isDone() && !g2.isDone()) return -1;
            if (!g1.isDone() && g2.isDone()) return 1;
            return Double.compare(g2.getCurrentDistance(), g1.getCurrentDistance());
        });

        return histories;
    }

    @Override
    public void generateGame(LocalDate date, double totalPrize) {
        // 자동 게임 생성 로직 필요 시 구현
    }

    @Override
    public GameType getGameType() {
        return GameType.SPEED;
    }
}
