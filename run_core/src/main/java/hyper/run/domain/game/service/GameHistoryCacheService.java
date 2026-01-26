package hyper.run.domain.game.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hyper.run.domain.game.dto.response.GameInProgressWatchResponse;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.repository.GameHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * 경기 상태 Redis 캐시 서비스
 * - 1위 상태: GameScheduler가 15초마다 rank 갱신 → rank=1 조회
 * - 사용자 상태: batch 업데이트 시 갱신
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameHistoryCacheService {

    private static final String FIRST_PLACE_STATUS_KEY_PREFIX = "game:first-status:";
    private static final String USER_STATUS_KEY_PREFIX = "game:user-status:";
    private static final Duration CACHE_TTL = Duration.ofHours(2);

    private final RedisTemplate<String, Object> jsonRedisTemplate;
    private final ObjectMapper redisObjectMapper;
    private final GameHistoryRepository gameHistoryRepository;


    public Optional<GameInProgressWatchResponse> getUserStatus(Long gameId, Long userId) {
        String key = USER_STATUS_KEY_PREFIX + gameId + ":" + userId;

        try {
            Object cached = jsonRedisTemplate.opsForValue().get(key);
            if (cached != null) {
                GameInProgressWatchResponse response = convertToStatusResponse(cached);
                if (response != null) {
                    return Optional.of(response);
                }
            }
        } catch (Exception e) {
            log.warn("Redis cache read failed for user status, gameId: {}, userId: {}", gameId, userId, e);
        }

        return findAndCacheUserStatus(gameId, userId);
    }

    public void updateUserStatusCache(Long gameId, Long userId, GameHistory gameHistory) {
        String key = USER_STATUS_KEY_PREFIX + gameId + ":" + userId;
        try {
            jsonRedisTemplate.opsForValue().set(key, GameInProgressWatchResponse.toResponse(gameHistory), CACHE_TTL);
        } catch (Exception e) {
            log.warn("Failed to cache user status for gameId: {}, userId: {}", gameId, userId, e);
        }
    }

    private Optional<GameInProgressWatchResponse> findAndCacheUserStatus(Long gameId, Long userId) {
        return gameHistoryRepository.findByUserIdAndGameId(userId, gameId)
                .map(gameHistory -> {
                    GameInProgressWatchResponse response = GameInProgressWatchResponse.toResponse(gameHistory);
                    String key = USER_STATUS_KEY_PREFIX + gameId + ":" + userId;
                    try {
                        jsonRedisTemplate.opsForValue().set(key, response, CACHE_TTL);
                    } catch (Exception e) {
                        log.warn("Failed to cache user status for gameId: {}, userId: {}", gameId, userId, e);
                    }
                    return response;
                });
    }

    public Optional<GameInProgressWatchResponse> getFirstPlaceStatus(Long gameId) {
        String key = FIRST_PLACE_STATUS_KEY_PREFIX + gameId;

        try {
            Object cached = jsonRedisTemplate.opsForValue().get(key);
            if (cached != null) {
                GameInProgressWatchResponse response = convertToStatusResponse(cached);
                if (response != null) {
                    return Optional.of(response);
                }
            }
        } catch (Exception e) {
        }

        return findAndCacheFirstPlaceStatus(gameId);
    }



    public void updateFirstPlaceCache(Long gameId) {
        gameHistoryRepository.findByGameIdAndRank(gameId, 1)
                .ifPresent(firstPlace -> cacheFirstPlaceStatus(gameId, GameInProgressWatchResponse.toResponse(firstPlace)));
    }


    public void evictCache(Long gameId) {
        try {
            jsonRedisTemplate.delete(FIRST_PLACE_STATUS_KEY_PREFIX + gameId);
        } catch (Exception e) {
            log.warn("Failed to evict cache for gameId: {}", gameId, e);
        }
    }

    /**
     * 캐시 미스 시 DB에서 rank=1 조회 후 캐시 저장
     */
    private Optional<GameInProgressWatchResponse> findAndCacheFirstPlaceStatus(Long gameId) {
        return gameHistoryRepository.findByGameIdAndRank(gameId, 1)
                .map(firstPlace -> {
                    GameInProgressWatchResponse response = GameInProgressWatchResponse.toResponse(firstPlace);
                    cacheFirstPlaceStatus(gameId, response);
                    return response;
                });
    }


    private void cacheFirstPlaceStatus(Long gameId, GameInProgressWatchResponse response) {
        String key = FIRST_PLACE_STATUS_KEY_PREFIX + gameId;
        try {
            jsonRedisTemplate.opsForValue().set(key, response, CACHE_TTL);
        } catch (Exception e) {
            log.warn("Failed to cache first place status for gameId: {}", gameId, e);
        }
    }


    private GameInProgressWatchResponse convertToStatusResponse(Object cached) {
        try {
            if (cached instanceof GameInProgressWatchResponse) {
                return (GameInProgressWatchResponse) cached;
            }
            return redisObjectMapper.convertValue(cached, GameInProgressWatchResponse.class);
        } catch (Exception e) {
            log.warn("Failed to convert cached object to status response", e);
            return null;
        }
    }
}
