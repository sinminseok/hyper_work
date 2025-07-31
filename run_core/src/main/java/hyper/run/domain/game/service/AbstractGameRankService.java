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
            User user = OptionalUtil.getOrElseThrow(userRepository.findById(history.getUserId()), hyper.run.exception.ErrorMessages.NOT_EXIST_USER_ID);

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

    protected abstract List<GameHistory> fetchSortedHistories(Game game);
}