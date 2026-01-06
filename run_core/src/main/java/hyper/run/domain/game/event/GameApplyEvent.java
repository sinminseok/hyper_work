package hyper.run.domain.game.event;

import hyper.run.domain.game.entity.GameDistance;

import java.time.LocalDateTime;

public record GameApplyEvent(
        Long userId,
        Long gameId,
        GameDistance gameDistance,
        Integer averageBpm,
        Integer targetCadence,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
    public static GameApplyEvent from(Long userId, Long gameId, GameDistance gameDistance, Integer averageBpm, Integer targetCadence, LocalDateTime startAt, LocalDateTime endAt) {
        return new GameApplyEvent(userId, gameId, gameDistance, averageBpm, targetCadence, startAt, endAt);
    }
}
