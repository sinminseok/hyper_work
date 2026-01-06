# WebSocket 실시간 순위 시스템 개선 방안

## 🚨 현재 구조의 문제점

### 1. 실시간 순위가 계산되지 않음

```java
// GameHistoryService.java:35-40
public GameInProgressWatchResponse updateGameHistory(final GameHistoryUpdateRequest request) {
    GameHistory gameHistory = getGameHistory(request);
    updateWatchConnection(gameHistory);
    updateGameHistoryFromRequest(gameHistory, request);
    return GameInProgressWatchResponse.toResponse(gameHistory); // ⚠️ rank는 항상 0
}
```

**문제점:**
- 워치에서 데이터를 받아도 순위를 계산하지 않음
- `GameInProgressWatchResponse`의 `rank` 필드가 항상 초기값(0)
- 순위 계산은 경기 종료 후에만 `calculateRank()` 호출

### 2. 순위 계산 성능 문제

```java
// AbstractGameRankService.java:32-37
public void calculateRank(Game game) {
    List<GameHistory> gameHistories = fetchSortedHistories(game); // MongoDB 전체 조회
    assignRanks(gameHistories);
    gameHistoryRepository.saveAll(gameHistories);
}
```

**문제점:**
- 매번 MongoDB에서 전체 참가자 조회 → O(N)
- 메모리에서 정렬 → O(N log N)
- 참가자 100명 × 5초마다 업데이트 = 초당 20회 정렬 연산

### 3. Redis 미활용

- Redis가 설정되어 있지만 인증 코드 저장에만 사용
- Redis Sorted Set을 사용하면 실시간 순위 O(log N) 가능

---

## ✅ 개선 방안

### 방안 1: Redis Sorted Set을 이용한 실시간 순위 (추천)

#### 장점
- **O(log N) 성능**: 1000명 참가자도 빠른 순위 조회
- **메모리 효율**: 경기 진행 중에만 Redis 사용, 종료 후 삭제
- **실시간성**: 워치에서 매번 최신 순위 받음

#### 구현 방식

```java
// 1. 데이터 수신 시 Redis에 점수 저장
@Transactional
public GameInProgressWatchResponse updateGameHistory(GameHistoryUpdateRequest request) {
    // MongoDB 업데이트
    GameHistory gameHistory = getGameHistory(request);
    updateGameHistoryFromRequest(gameHistory, request);

    // Redis에 점수 저장 (경기 타입별 점수 계산)
    Game game = gameRepository.findById(request.getGameId()).get();
    double score = calculateScore(game.getType(), gameHistory);
    String redisKey = "game:rank:" + request.getGameId();
    redisTemplate.opsForZSet().add(redisKey, String.valueOf(request.getUserId()), score);

    // Redis에서 실시간 순위 조회 (O(log N))
    Long rank = redisTemplate.opsForZSet().reverseRank(redisKey, String.valueOf(request.getUserId()));
    gameHistory.setRank(rank != null ? rank.intValue() + 1 : 0);

    return GameInProgressWatchResponse.toResponse(gameHistory);
}

// 2. 점수 계산 (경기 타입별)
private double calculateScore(GameType gameType, GameHistory history) {
    return switch (gameType) {
        case SPEED -> history.isDone() ?
            999999 - history.getRemainingDistance() : // 완주자 우선, 남은 거리 적을수록 높은 점수
            history.getCurrentDistance(); // 미완주자는 현재 거리
        case CADENCE -> history.isDone() ?
            999999 - history.getCadenceScore() : // 완주자 우선, 목표와 차이 적을수록 높은 점수
            -history.getCadenceScore();
        case HEARTBEAT -> history.isDone() ?
            999999 - history.getHeartBeatScore() :
            -history.getHeartBeatScore();
    };
}

// 3. 경기 종료 시 Redis 데이터 삭제
public void finishGame(Long gameId) {
    String redisKey = "game:rank:" + gameId;
    redisTemplate.delete(redisKey); // TTL 설정 대신 명시적 삭제
}
```

#### Redis 데이터 구조

```redis
# Key: game:rank:{gameId}
# Type: Sorted Set
# Score: 점수 (높을수록 상위 순위)
# Member: userId

ZADD game:rank:123 1250.5 456    # userId=456, 현재거리=1250.5m
ZADD game:rank:123 1180.2 789    # userId=789, 현재거리=1180.2m
ZREVRANK game:rank:123 456       # → 0 (1등)
ZREVRANGE game:rank:123 0 9      # → 상위 10명
```

---

### 방안 2: 브로드캐스트 방식 (선택적)

현재는 개인에게만 응답을 보내는데, 전체 순위 변동을 브로드캐스트할 수도 있습니다:

```java
@MessageMapping(value = "/game/update")
public void sendMessage(final GameHistoryUpdateRequest request) {
    // 1. 개인 데이터 업데이트 및 순위 계산
    GameInProgressWatchResponse myResponse = service.updateGameHistory(request);

    // 2. 개인 응답 전송 (현재 방식 유지)
    template.convertAndSend(
        "/sub/game/my/" + request.getGameId() + "/" + request.getUserId(),
        myResponse
    );

    // 3. (선택) 상위 10명 순위 브로드캐스트 → 모든 참가자에게
    List<RankInfo> top10 = service.getTop10Ranks(request.getGameId());
    template.convertAndSend(
        "/sub/game/rank/" + request.getGameId(),
        top10
    );
}
```

**장점**: 다른 참가자 순위도 볼 수 있음
**단점**: 네트워크 트래픽 증가 (참가자 × 업데이트 빈도)

---

### 방안 3: WebSocket 인증 강화

현재 WebSocket 연결 시 인증이 없는 것으로 보입니다. 보안 강화가 필요합니다:

```java
@Configuration
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtService jwtService;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = accessor.getFirstNativeHeader("Authorization");
                    if (token != null && token.startsWith("Bearer ")) {
                        String jwt = token.substring(7);
                        String email = jwtService.extractEmail(jwt);
                        accessor.setUser(() -> email);
                    } else {
                        throw new IllegalArgumentException("Missing or invalid token");
                    }
                }
                return message;
            }
        });
    }
}
```

---

### 방안 4: 배치 업데이트 최적화 (고급)

여러 사용자의 데이터가 동시에 들어올 때 배치 처리:

```java
// 100ms 마다 모아서 한번에 처리
@Scheduled(fixedDelay = 100)
public void processBatch() {
    List<GameHistoryUpdateRequest> batch = requestQueue.drainToList();
    if (batch.isEmpty()) return;

    // 배치로 Redis 업데이트
    batch.forEach(req -> {
        // Redis Pipeline 사용
        redisTemplate.executePipelined(...);
    });
}
```

**장점**: Redis 요청 횟수 감소
**단점**: 구현 복잡도 증가, 100ms 지연

---

## 🎯 추천 구현 순서

1. **Redis Sorted Set 실시간 순위** (방안 1) - 필수
2. **WebSocket 인증** (방안 3) - 필수
3. 브로드캐스트 (방안 2) - 선택
4. 배치 최적화 (방안 4) - 나중에

---

## 📊 성능 비교

| 방식 | 시간 복잡도 | 100명 참가 시 | 1000명 참가 시 |
|------|------------|--------------|----------------|
| **현재 (MongoDB 정렬)** | O(N log N) | ~10ms | ~100ms |
| **Redis Sorted Set** | O(log N) | ~0.1ms | ~0.2ms |

### 실제 사용 시나리오

워치에서 5초마다 업데이트하고, 100명이 동시에 경기하면:
- **현재**: 초당 20회 × 10ms = 200ms CPU 사용
- **Redis**: 초당 20회 × 0.1ms = 2ms CPU 사용

---

## 🔧 구현 시 주의사항

### 1. Redis 메모리 관리
- 경기 시작 시: Redis에 Sorted Set 생성
- 경기 종료 시: Redis 데이터 명시적 삭제
- TTL 설정: 경기 종료 후 24시간 뒤 자동 삭제 (백업용)

### 2. 점수 계산 정확성
- 완주자(isDone=true)는 항상 미완주자보다 높은 점수
- 스피드: 남은 거리가 적을수록 높은 순위
- 케이던스/심박수: 목표 값과 차이가 적을수록 높은 순위

### 3. 동시성 처리
- Redis는 Single Thread라 별도 락 불필요
- MongoDB 업데이트와 Redis 업데이트는 별도 트랜잭션
- Redis 실패 시에도 MongoDB는 업데이트 되어야 함

### 4. 경기 종료 후 최종 순위
- Redis의 실시간 순위와 MongoDB의 최종 순위 일치 보장
- 경기 종료 시 기존 `calculateRank()` 메서드로 최종 검증
- 불일치 발견 시 로깅 및 알림

---

## 🧪 테스트 케이스

### 1. 실시간 순위 정확성
- 2명 참가, A가 B보다 앞서는 경우
- 100명 참가, 순위 변동 테스트
- 완주자와 미완주자 혼재 시 순위

### 2. 성능 테스트
- 100명 동시 접속, 5초마다 업데이트
- Redis 응답 시간 측정
- MongoDB 쓰기 지연 모니터링

### 3. 장애 상황
- Redis 연결 끊김 시 처리
- WebSocket 재연결 시 순위 복구
- 경기 중 서버 재시작 시나리오

---

## 📝 관련 파일

### 수정 필요
- `run_core/src/main/java/hyper/run/domain/game/service/GameHistoryService.java`
- `run_api/src/main/java/hyper/run/config/StompWebSocketConfig.java`
- `run_api/src/main/java/hyper/run/game/GameWebSocketController.java`

### 신규 생성
- `run_core/src/main/java/hyper/run/domain/game/service/GameRankRedisService.java`
- `run_core/src/main/java/hyper/run/domain/game/dto/response/RankInfo.java`
