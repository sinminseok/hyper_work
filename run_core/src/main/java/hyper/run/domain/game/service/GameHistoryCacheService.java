package hyper.run.domain.game.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hyper.run.domain.game.dto.response.GameInProgressWatchResponse;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

import static hyper.run.exception.ErrorMessages.NOT_EXIST_GAME_ID;

/**
 * 경기 상태 Redis 캐시 서비스 (ZSet + Hash 구조)
 *
 * Redis 키 구조:
 * - game:rank:{gameId}  → ZSet (score: rank 값, member: userId)
 * - game:data:{gameId}  → Hash (field: userId, value: GameInProgressWatchResponse JSON)
 *
 * 순위 산정:
 * - GameRankService가 15초마다 경기 타입별 규칙에 따라 순위 계산
 * - SPEED: 완주 여부 → 소요 시간 → 남은 거리
 * - CADENCE: 완주 여부 → 케이던스 점수 → 소요 시간
 * - HEARTBEAT: 완주 여부 → 심박수 점수 → 소요 시간
 * - 계산된 rank 값을 ZSet score로 사용 (낮을수록 높은 순위)
 *
 * 장점:
 * - 순위 조회: O(log N)
 * - 키 수: 경기당 2개로 고정
 * - 전체 순위 조회 가능
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameHistoryCacheService {

    private static final String RANK_KEY_PREFIX = "game:rank:";
    private static final String DATA_KEY_PREFIX = "game:data:";
    private static final Duration CACHE_TTL = Duration.ofHours(2);

    // 순위 미정(rank=0)인 경우 사용할 score (최하위로 밀어냄)
    private static final int UNRANKED_SCORE = Integer.MAX_VALUE;

    private final RedisTemplate<String, Object> jsonRedisTemplate;
    private final ObjectMapper redisObjectMapper;
    private final GameHistoryRepository gameHistoryRepository;


    /**
     * 사용자 상태 조회 (캐시 → DB fallback)
     */
    public GameInProgressWatchResponse getUserStatus(Long gameId, Long userId) {
        String key = DATA_KEY_PREFIX + gameId;

        try {
            Object cached = jsonRedisTemplate.opsForHash().get(key, userId.toString());
            if (cached != null) {
                GameInProgressWatchResponse response = convertToResponse(cached);
                if (response != null) {
                    return response;
                }
            }
        } catch (Exception e) {
            log.warn("Redis cache read failed for user status, gameId: {}, userId: {}", gameId, userId, e);
        }

        return findAndCacheUserStatus(gameId, userId);
    }

    /**
     * 1위 상태 조회 (ZSet에서 rank=1 사용자 조회 → Hash에서 데이터 조회)
     * - score가 낮을수록 높은 순위 (rank=1이 score=1)
     */
    public GameInProgressWatchResponse getFirstPlaceStatus(Long gameId) {
        String rankKey = RANK_KEY_PREFIX + gameId;
        String dataKey = DATA_KEY_PREFIX + gameId;

        try {
            Set<Object> firstPlaceSet = jsonRedisTemplate.opsForZSet().range(rankKey, 0, 0);

            if (firstPlaceSet != null && !firstPlaceSet.isEmpty()) {
                String firstUserId = firstPlaceSet.iterator().next().toString();
                Object cached = jsonRedisTemplate.opsForHash().get(dataKey, firstUserId);

                if (cached != null) {
                    GameInProgressWatchResponse response = convertToResponse(cached);
                    if (response != null) {
                        return response;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Redis cache read failed for first place, gameId: {}", gameId, e);
        }

        return findAndCacheFirstPlaceStatus(gameId);
    }


    /**
     * 사용자 상태 캐시 업데이트 (ZSet 순위 + Hash 데이터)
     *
     * ZSet score:
     * - GameRankService가 계산한 rank 값 사용
     * - rank=0 (순위 미정)인 경우 UNRANKED_SCORE 사용하여 최하위로 배치
     * - 낮은 score = 높은 순위 (rank=1이 1위)
     */
    public void updateUserStatusCache(Long gameId, Long userId, GameHistory gameHistory) {
        String rankKey = RANK_KEY_PREFIX + gameId;
        String dataKey = DATA_KEY_PREFIX + gameId;

        try {
            GameInProgressWatchResponse response = GameInProgressWatchResponse.toResponse(gameHistory);

            // ZSet에 순위 점수 업데이트 (rank 값 사용, 0이면 최하위)
            int rank = gameHistory.getRank();
            double score = (rank > 0) ? rank : UNRANKED_SCORE;
            jsonRedisTemplate.opsForZSet().add(rankKey, userId.toString(), score);

            // Hash에 상세 데이터 업데이트
            jsonRedisTemplate.opsForHash().put(dataKey, userId.toString(), response);

            // TTL 설정 (키가 처음 생성될 때만)
            refreshTTLIfNeeded(rankKey);
            refreshTTLIfNeeded(dataKey);

        } catch (Exception e) {
            log.warn("Failed to update user status cache, gameId: {}, userId: {}", gameId, userId, e);
        }
    }

    /**
     * 경기 캐시 삭제 (경기 종료 시 호출)
     */
    public void evictCache(Long gameId) {
        String rankKey = RANK_KEY_PREFIX + gameId;
        String dataKey = DATA_KEY_PREFIX + gameId;

        try {
            jsonRedisTemplate.delete(rankKey);
            jsonRedisTemplate.delete(dataKey);
            log.info("Evicted cache for gameId: {}", gameId);
        } catch (Exception e) {
            log.warn("Failed to evict cache for gameId: {}", gameId, e);
        }
    }

    private GameInProgressWatchResponse findAndCacheUserStatus(Long gameId, Long userId) {
        GameHistory gameHistory = OptionalUtil.getOrElseThrow(gameHistoryRepository.findByUserIdAndGameId(userId, gameId), NOT_EXIST_GAME_ID);
        updateUserStatusCache(gameId, userId, gameHistory);
        return GameInProgressWatchResponse.toResponse(gameHistory);
    }

    private GameInProgressWatchResponse findAndCacheFirstPlaceStatus(Long gameId) {
        GameHistory firstPlace = OptionalUtil.getOrElseThrow(gameHistoryRepository.findByGameIdAndRank(gameId, 1), NOT_EXIST_GAME_ID);
        updateUserStatusCache(gameId, firstPlace.getUserId(), firstPlace);
        return GameInProgressWatchResponse.toResponse(firstPlace);
    }

    private void refreshTTLIfNeeded(String key) {
        try {
            Long ttl = jsonRedisTemplate.getExpire(key);
            if (ttl == null || ttl == -1) {
                jsonRedisTemplate.expire(key, CACHE_TTL);
            }
        } catch (Exception e) {
            log.warn("Failed to refresh TTL for key: {}", key, e);
        }
    }

    private GameInProgressWatchResponse convertToResponse(Object cached) {
        try {
            if (cached instanceof GameInProgressWatchResponse) {
                return (GameInProgressWatchResponse) cached;
            }
            return redisObjectMapper.convertValue(cached, GameInProgressWatchResponse.class);
        } catch (Exception e) {
            log.warn("Failed to convert cached object to response", e);
            return null;
        }
    }
}
