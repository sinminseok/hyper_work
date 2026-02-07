package hyper.run.domain.game.event;

import java.time.LocalDate;
import java.util.List;

public record GameExpiredCleanupEvent(
        LocalDate targetDate,
        List<Long> gameIds
) {
    public static GameExpiredCleanupEvent from(LocalDate targetDate, List<Long> gameIds) {
        return new GameExpiredCleanupEvent(targetDate, gameIds);
    }
}
