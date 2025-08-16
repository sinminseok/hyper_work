package hyper.run.domain.game.service;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;

import java.util.Comparator;
import java.util.List;

public abstract class AbstractGameRankService implements GameRankService {

    protected final GameHistoryRepository gameHistoryRepository;
    protected final UserRepository userRepository;

    protected AbstractGameRankService(GameHistoryRepository gameHistoryRepository, UserRepository userRepository) {
        this.gameHistoryRepository = gameHistoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void calculateRank(Game game) {
        List<GameHistory> gameHistories = fetchSortedHistories(game);
        assignRanks(gameHistories);
        gameHistoryRepository.saveAll(gameHistories);
    }

    @Override
    public void saveGameResult(Game game) {
        List<GameHistory> gameHistories = fetchSortedDoneHistories(game);
        setAllDone(gameHistories);
        distributePrizes(game, gameHistories);
        gameHistoryRepository.saveAll(gameHistories);
    }

    private void setAllDone(List<GameHistory> histories) {
        histories.forEach(history -> history.setDone(true));
    }

    private List<GameHistory> fetchSortedDoneHistories(Game game) {
        return gameHistoryRepository.findAllByGameId(game.getId()).stream()
                .filter(h -> h.getRank() != 0)
                .sorted(Comparator.comparingInt(GameHistory::getRank))
                .toList();
    }

    private void assignRanks(List<GameHistory> histories) {
        int rank = 1;
        for (GameHistory history : histories) {
            if (!history.isDone()) {
                history.setRank(rank++);
            }
        }
    }

    private void distributePrizes(Game game, List<GameHistory> histories) {
        int limit = Math.min(3, histories.size());
        for (int i = 0; i < limit; i++) {
            GameHistory history = histories.get(i);
            User user = OptionalUtil.getOrElseThrow(
                    userRepository.findById(history.getUserId()),
                    hyper.run.exception.ErrorMessages.NOT_EXIST_USER_ID
            );
            double prize = calculatePrizeForRank(game, i);
            awardPrizeToUser(history, user, prize);
            assignWinnerName(game, i, user);
        }
    }


    private double calculatePrizeForRank(Game game, int rank) {
        return switch (rank) {
            case 0 -> game.getFirstPlacePrize();
            case 1 -> game.getSecondPlacePrize();
            case 2 -> game.getThirdPlacePrize();
            default -> 0;
        };
    }

    private void assignWinnerName(Game game, int rank, User user) {
        switch (rank) {
            case 0 -> game.setFirstUserName(user.getName());
            case 1 -> game.setSecondUserName(user.getName());
            case 2 -> game.setThirdUserName(user.getName());
        }
    }

    private void awardPrizeToUser(GameHistory history, User user, double prize) {
        user.increasePoint(prize);
        history.setPrize(prize);
    }

    //각 경기별 순위(랭킹)를 산정하는 방식이 다르다(즉 정렬 방식이 다르다)
    protected abstract List<GameHistory> fetchSortedHistories(Game game);
}