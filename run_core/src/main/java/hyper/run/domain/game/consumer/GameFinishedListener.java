package hyper.run.domain.game.consumer;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.event.GameFinishedEvent;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Comparator;
import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class GameFinishedListener {

    private final GameHistoryRepository gameHistoryRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleGameFinished(GameFinishedEvent event) {
        Game game = OptionalUtil.getOrElseThrow(gameRepository.findById(event.getGameId()), "Game not found");

        List<GameHistory> rankedHistories = gameHistoryRepository
                .findAllByGameId(game.getId()).stream()
                .filter(h -> h.getRank() != 0)
                .sorted(Comparator.comparingInt(GameHistory::getRank))
                .toList();

        if (rankedHistories.isEmpty()) {
            return;
        }

        rankedHistories.forEach(history -> history.setDone(true));

        // 1~20위까지 상금 지급 (완주자만)
        List<GameHistory> topRankers = rankedHistories.stream()
                .limit(20)
                .toList();

        for (int i = 0; i < topRankers.size(); i++) {
            GameHistory history = topRankers.get(i);
            User user = userRepository.findById(history.getUserId()).orElseThrow(() -> new IllegalStateException("User not found: " + history.getUserId()));

            // 완주자만 상금 지급
            if (history.isDone()) {
                double prize = calculatePrizeForRank(game, i);
                if (prize > 0) {
                    user.increasePoint(prize);
                    history.setPrize(prize);
                }
            }

            // 상위 3명의 이름은 기록
            if (i < 3) {
                assignWinnerName(game, i, user);
            }
        }

        gameHistoryRepository.saveAll(rankedHistories);
    }


    private double calculatePrizeForRank(Game game, int rankIndex) {
        return switch (rankIndex) {
            case 0 -> game.getFirstPlacePrize();      // 1위: 60%
            case 1 -> game.getSecondPlacePrize();     // 2위: 15%
            case 2 -> game.getThirdPlacePrize();      // 3위: 6%
            case 3 -> game.getFourthPlacePrize();     // 4위: 3%
            case 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 -> game.getOtherPlacePrize();  // 5~20위: 1%
            default -> 0;  // 21위 이상: 상금 없음
        };
    }

    private void assignWinnerName(Game game, int rankIndex, User user) {
        switch (rankIndex) {
            case 0 -> game.setFirstUserName(user.getName());
            case 1 -> game.setSecondUserName(user.getName());
            case 2 -> game.setThirdUserName(user.getName());
        }
    }

}
