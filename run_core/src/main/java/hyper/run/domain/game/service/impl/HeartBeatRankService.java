package hyper.run.domain.game.service.impl;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameDistance;
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
import java.util.Comparator;
import java.util.List;

import static hyper.run.exception.ErrorMessages.NOT_EXIST_USER_ID;

/**
 * 사용자가 설정한 BPM(심박수)에 가까울수록 높은 점수
 */
@Service
@RequiredArgsConstructor
public class HeartBeatRankService implements GameRankService {

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
    public void generateGame(LocalDate date, double totalPrize) {

    }

    @Override
    public void saveGameResult(Game game) {
        List<GameHistory> gameHistories = fetchSortedDoneHistories(game);
        setAllDone(gameHistories);
        distributePrizes(game, gameHistories);
        gameHistoryRepository.saveAll(gameHistories);
    }

    @Override
    public GameType getGameType() {
        return GameType.HEARTBEAT;
    }

    private void setAllDone(List<GameHistory> histories) {
        histories.forEach(gameHistory -> gameHistory.setDone(true));
    }

    private List<GameHistory> fetchSortedDoneHistories(Game game) {
        List<GameHistory> histories = gameHistoryRepository.findAllByGameId(game.getId());
        return histories.stream()
                .sorted(Comparator.comparingInt((GameHistory g) -> g.getRank() == 0 ? Integer.MAX_VALUE : g.getRank()))
                .toList();
    }

    private List<GameHistory> fetchSortedHistories(Game game) {
        List<GameHistory> histories = gameHistoryRepository.findAllByGameId(game.getId());

        histories.sort((g1, g2) -> {
            if (g1.isDone() && !g2.isDone()) return -1;
            if (!g1.isDone() && g2.isDone()) return 1;
            return Double.compare(g1.calculateHeartBeatScore(), g2.calculateHeartBeatScore());
        });

        return histories;
    }


    private void assignRanks(List<GameHistory> histories) {
        for (int i = 0; i < histories.size(); i++) {
            GameHistory history = histories.get(i);
            if (!history.isDone()) {
                history.setRank(i + 1);
            }
        }
    }

    private void distributePrizes(Game game, List<GameHistory> histories) {
        for (int i = 0; i < Math.min(3, histories.size()); i++) {
            GameHistory history = histories.get(i);
            User user = OptionalUtil.getOrElseThrow(userRepository.findById(history.getUserId()), NOT_EXIST_USER_ID);

            double prize = switch (i) {
                case 0 -> game.getFirstPlacePrize();
                case 1 -> game.getSecondPlacePrize();
                case 2 -> game.getThirdPlacePrize();
                default -> 0;
            };

            user.increasePoint(prize);
            history.setPrize(prize);
            history.setDone(true);

            switch (i) {
                case 0 -> game.setFirstUserName(user.getName());
                case 1 -> game.setSecondUserName(user.getName());
                case 2 -> game.setThirdUserName(user.getName());
            }
        }
    }
}