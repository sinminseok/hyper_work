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
import java.util.List;

import static hyper.run.domain.game.utils.GamePrizeCalculator.*;
import static hyper.run.exception.ErrorMessages.NOT_EXIST_USER_ID;

@RequiredArgsConstructor
@Service
public class CadenceRankService implements GameRankService {

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
        histories.sort((g1, g2) -> {
            if (g1.isDone() && !g2.isDone()) return -1;
            if (!g1.isDone() && g2.isDone()) return 1;
            return Double.compare(g1.calculateCadenceScore(), g2.calculateCadenceScore());
        });
        return histories;
    }

    private void assignRanks(List<GameHistory> histories) {
        for(int i=0; i < histories.size(); i++){
            GameHistory history = histories.get(i);
            if(!history.isDone()) {
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

    @Override
    public void generateGame(LocalDate date, double totalPrize) {
        for (GameDistance distance : GameDistance.values()) {
            for (int i = 5; i <= 23; i += distance.getTime()) {
                if (i + distance.getTime() > 24) break;
                Game game = createGame(distance, date, i, totalPrize);
                gameRepository.save(game);
            }
        }
    }

    private Game createGame(GameDistance distance, LocalDate date, int startTime, double totalPrize) {
        return Game.builder()
                .name(GameType.CADENCE.getName() + "-" + distance.getName())
                .type(GameType.CADENCE)
                .distance(distance)
                .gameDate(date)
                .startAt(date.atTime(startTime, 0))
                .endAt(date.atTime(startTime, 0).plusHours(distance.getTime()))
                .participatedCount(0)
                .totalPrize(totalPrize)
                .firstPlacePrize(calculateFirstPlacePrize(totalPrize))
                .secondPlacePrize(calculateSecondPlacePrize(totalPrize))
                .thirdPlacePrize(calculateThirdPlacePrize(totalPrize))
                .build();
    }

    @Override
    public GameType getGameType() {
        return GameType.CADENCE;
    }
}
