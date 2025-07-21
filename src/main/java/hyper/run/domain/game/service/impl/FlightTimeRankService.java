package hyper.run.domain.game.service.impl;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.domain.game.service.GameRankService;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FlightTimeRankService implements GameRankService {

    private final GameRepository gameRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final UserRepository userRepository;

    @Override
    public void calculateRank(Game game) {
        List<GameHistory> gameHistories = fetchSortedHistories(game);
        assignRanks(gameHistories);
        gameHistoryRepository.saveAll(gameHistories);
    }

    @Override
    public void saveGameResult(Game game) {
        List<GameHistory> gameHistories = fetchSortedHistories(game);
        assignRanks(gameHistories);
        distributePrizes(game, gameHistories);
        gameHistoryRepository.saveAll(gameHistories);
    }

    private List<GameHistory> fetchSortedHistories(Game game) {
        List<GameHistory> histories = gameHistoryRepository.findAllByGameId(game.getId());
        histories.sort((g1, g2) -> Double.compare(g2.getCurrentFlightTime(), g1.getCurrentFlightTime())); // 내림차순
        return histories;
    }

    private void assignRanks(List<GameHistory> histories) {
        int rank = 1;
        for (GameHistory history : histories) {
            history.setRank(rank++);
        }
    }

    private void distributePrizes(Game game, List<GameHistory> histories) {
        for (int i = 0; i < Math.min(3, histories.size()); i++) {
            GameHistory history = histories.get(i);
            User user = OptionalUtil.getOrElseThrow(userRepository.findById(history.getUserId()), "존재하지 않는 사용자 아이디 입니다.");

            double prize = switch (i) {
                case 0 -> game.getFirstPlacePrize();
                case 1 -> game.getSecondPlacePrize();
                case 2 -> game.getThirdPlacePrize();
                default -> 0;
            };

            user.increasePoint(prize);
            history.setPrize(prize);

            switch (i) {
                case 0 -> game.setFirstUserName(user.getName());
                case 1 -> game.setSecondUserName(user.getName());
                case 2 -> game.setThirdUserName(user.getName());
            }
        }
    }

    @Override
    public void generateGame(LocalDate date, double totalPrize) {
        // 추후 구현
    }

    @Override
    public GameType getGameType() {
        return GameType.FLIGHT_TIME;
    }
}
