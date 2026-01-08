package hyper.run.domain.game.service;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.event.GameFinishedEvent;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.utils.OptionalUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

import static hyper.run.exception.ErrorMessages.NOT_EXIST_GAME_ID;

public abstract class AbstractGameRankService implements GameRankService {

    private final ApplicationEventPublisher applicationEventPublisher;
    protected final GameHistoryRepository gameHistoryRepository;
    protected final GameRepository gameRepository;

    protected AbstractGameRankService(
            ApplicationEventPublisher applicationEventPublisher,
            GameHistoryRepository gameHistoryRepository,
            GameRepository gameRepository) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.gameHistoryRepository = gameHistoryRepository;
        this.gameRepository = gameRepository;
    }

    @Override
    public void calculateRank(Game game) {
        List<GameHistory> gameHistories = fetchSortedHistories(game);
        assignRanks(gameHistories);
        gameHistoryRepository.saveAll(gameHistories);
    }


    @Override
    @Transactional
    public void saveGameResult(Game game) {
        GameFinishedEvent event = GameFinishedEvent.from(game);
        applicationEventPublisher.publishEvent(event);
    }

    private void assignRanks(List<GameHistory> histories) {
        int rank = 1;
        for (GameHistory history : histories) {
            history.setRank(rank++);  // 모든 참가자에게 순위 부여
        }
    }

    // 각 경기별 순위 산정 방식 다름 → 정렬 방식 추상화
    protected abstract List<GameHistory> fetchSortedHistories(Game game);
}
