package hyper.run.domain.game.repository;

import hyper.run.domain.game.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GameRepositoryCustom {
    Page<Game> findGamesByCriteria(
            LocalDateTime createdAfter,
            LocalDateTime createdBefore,
            AdminGameStatus status,
            String keyword,
            Pageable pageable
    );

    List<Game> findGamesOrderByPrizeWithoutCursor(LocalDateTime now, int limit);

    List<Game> findGamesOrderByPrizeWithCursor(LocalDateTime now, double cursorTotalPrize, Long cursorGameId, int limit);

    Optional<Game> findByGameConditions(LocalDateTime startAt, GameDistance distance, GameType type, ActivityType activityType);

    List<Game> findGamesByYearAndMonth(int year, int month, List<Long> gameIds);
}
