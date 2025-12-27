package hyper.run.domain.game.event;

import hyper.run.domain.game.entity.GameDistance;

public record GameApplyEvent(
        Long userId,
        Long gameId,
        GameDistance gameDistance,
        Integer averageBpm,
        Integer targetCadence
) {
    public static GameApplyEvent from(Long userId, Long gameId, GameDistance gameDistance, Integer averageBpm, Integer targetCadence) {
        return new GameApplyEvent(userId, gameId, gameDistance, averageBpm, targetCadence);
    }
}
