# HTTP Polling 최적화 가이드

## 배경

애플 워치는 WebSocket을 지원하지 않아 HTTP Polling 방식을 사용해야 한다.
현재 5초마다 생체 데이터를 업데이트하면 동시 접속자가 많을 경우 트래픽 부하가 발생할 수 있다.

**예시**: 동시 접속자 1,000명 x 5초마다 = **분당 12,000건** 요청

---

## 현재 구현 상태

### HTTP Polling (Apple Watch용)

**엔드포인트**: `PATCH /v1/api/game-histories`

```java
@PatchMapping
public ResponseEntity<?> updateGameHistory(@RequestBody final GameHistoryUpdateRequest gameHistoryUpdateRequest){
    gameHistoryService.updateAsyncGameHistory(gameHistoryUpdateRequest);
    SuccessResponse response = new SuccessResponse(true, "내 생체 데이터 정보 업데이트", null);
    return new ResponseEntity<>(response, HttpStatus.OK);
}
```

- `@Async`로 비동기 처리
- 응답을 즉시 반환하고 백그라운드에서 MongoDB 저장

### WebSocket (Galaxy Watch, WearOS용)

**엔드포인트**: `/pub/game/update`

- 동기 처리로 실시간 응답
- 순위 정보 포함하여 클라이언트에 전송

---

## 최적화 방안

### 1. 배치 업데이트 (권장)

클라이언트에서 여러 데이터를 모아 한 번에 전송한다.

**기존**: 5초마다 1건씩 전송
**개선**: 30초마다 6건을 한 번에 전송

```java
// Controller
@PatchMapping("/batch")
public ResponseEntity<?> updateGameHistoryBatch(
    @RequestBody List<GameHistoryUpdateRequest> requests) {
    gameHistoryService.updateBatchAsync(requests);
    return ResponseEntity.ok(new SuccessResponse(true, "배치 업데이트 완료", null));
}

// Service
@Async
@Transactional
public void updateBatchAsync(List<GameHistoryUpdateRequest> requests) {
    if (requests.isEmpty()) return;

    // 같은 gameId, userId의 요청들을 하나로 병합
    GameHistoryUpdateRequest merged = mergeRequests(requests);

    GameHistory gameHistory = getGameHistory(merged);
    updateWatchConnection(gameHistory);

    // 배치 데이터 처리
    for (GameHistoryUpdateRequest request : requests) {
        gameHistory.updateFrom(request);
    }

    gameHistory.checkDoneByDistance();
    repository.save(gameHistory);
}
```

**효과**: 요청 수 **6배 감소**

---

### 2. Redis 버퍼 + 비동기 저장

실시간 데이터는 Redis에 저장하고, MongoDB에는 주기적으로 배치 저장한다.

```
[Apple Watch] → [API Server] → [Redis (실시간 버퍼)]
                                       ↓ (5초마다 배치)
                                  [MongoDB (영구 저장)]
```

**구현 예시**:

```java
// Service
@Async
public void updateAsyncGameHistory(GameHistoryUpdateRequest request) {
    String key = "game:" + request.getGameId() + ":user:" + request.getUserId();

    // Redis에 즉시 저장
    redisTemplate.opsForValue().set(key, request);

    // 배치 저장 대상 큐에 추가
    redisTemplate.opsForList().rightPush("pending_updates", key);
}

// Scheduler - 5초마다 실행
@Scheduled(fixedRate = 5000)
public void flushToMongoDB() {
    List<String> keys = redisTemplate.opsForList().range("pending_updates", 0, -1);
    redisTemplate.delete("pending_updates");

    for (String key : keys) {
        GameHistoryUpdateRequest request = redisTemplate.opsForValue().get(key);
        // MongoDB에 저장
        gameHistoryService.persistToMongo(request);
    }
}
```

**장점**:
- DB 쓰기 부하 대폭 감소
- 순위 조회는 Redis에서 빠르게 처리 가능

---

### 3. 동적 폴링 주기

경기 상황에 따라 폴링 주기를 동적으로 조절한다.

| 상황 | 폴링 주기 | 설명 |
|------|----------|------|
| 경기 시작 직후 | 5초 | 초기 데이터 수집 |
| 일반 진행 중 | 10초 | 기본 주기 |
| 완주 임박 (목표 거리 90% 이상) | 3초 | 정밀 측정 |
| 완주 후 | 업데이트 중단 | 불필요한 요청 제거 |

**응답에 다음 폴링 주기 포함**:

```java
@Getter
@Builder
public class GameHistoryUpdateResponse {
    private boolean success;
    private int nextPollIntervalSeconds;  // 다음 폴링 주기 (초)
    private boolean isDone;

    public static GameHistoryUpdateResponse of(GameHistory history) {
        int interval = calculateInterval(history);
        return GameHistoryUpdateResponse.builder()
            .success(true)
            .nextPollIntervalSeconds(interval)
            .isDone(history.isDone())
            .build();
    }

    private static int calculateInterval(GameHistory history) {
        if (history.isDone()) {
            return -1;  // 더 이상 폴링 불필요
        }

        double progress = history.getCurrentDistance() / history.getGameDistance().getDistance();

        if (progress >= 0.9) {
            return 3;   // 완주 임박
        } else if (progress <= 0.1) {
            return 5;   // 시작 직후
        } else {
            return 10;  // 일반 진행
        }
    }
}
```

**Apple Watch 클라이언트**:

```swift
func sendBioData() {
    apiClient.updateGameHistory(request) { response in
        if response.isDone {
            self.stopPolling()
        } else {
            self.scheduleNextPoll(seconds: response.nextPollIntervalSeconds)
        }
    }
}
```

---

### 4. 클라이언트 측 평균 계산

현재 서버에서 누적 평균을 계산하고 있다. 이를 클라이언트로 이동하면 서버 부하가 감소한다.

**기존 (서버 측 평균 계산)**:
- 매 요청마다 `updateCount` 기반 평균 계산
- 모든 업데이트 필요

**개선 (클라이언트 측 평균 계산)**:
- 클라이언트가 5초마다 측정한 값의 평균을 계산
- 30초마다 평균값만 서버로 전송

```swift
// Apple Watch
class BioDataCollector {
    private var bpmSamples: [Double] = []
    private var cadenceSamples: [Double] = []

    func collectSample(bpm: Double, cadence: Double) {
        bpmSamples.append(bpm)
        cadenceSamples.append(cadence)
    }

    func flush() -> GameHistoryUpdateRequest {
        let avgBpm = bpmSamples.reduce(0, +) / Double(bpmSamples.count)
        let avgCadence = cadenceSamples.reduce(0, +) / Double(cadenceSamples.count)

        bpmSamples.removeAll()
        cadenceSamples.removeAll()

        return GameHistoryUpdateRequest(
            currentBpm: avgBpm,
            currentCadence: avgCadence,
            // ... 기타 필드
        )
    }
}
```

---

### 5. HTTP/2 + Keep-Alive 활성화

연결 재사용으로 핸드셰이크 오버헤드를 줄인다.

```yaml
# application.yml
server:
  http2:
    enabled: true

spring:
  mvc:
    async:
      request-timeout: 30000
```

---

## 권장 구현 조합

가장 효과적인 조합은 **1번 + 3번 + 4번**이다.

### 구현 순서

1. **동적 폴링 주기** (3번) 먼저 적용
   - 서버 변경 최소화
   - 즉각적인 트래픽 감소 효과

2. **클라이언트 측 평균 계산** (4번) 적용
   - 클라이언트 업데이트 필요
   - 서버 로직 단순화

3. **배치 업데이트** (1번) 적용
   - 새로운 엔드포인트 추가
   - 요청 수 대폭 감소

### 예상 효과

| 최적화 | 요청 감소율 | 난이도 |
|--------|-----------|--------|
| 동적 폴링 주기 | 약 30-50% | 낮음 |
| 클라이언트 측 평균 계산 | - | 중간 |
| 배치 업데이트 | 약 80% | 중간 |
| **조합 적용 시** | **약 90%** | - |

**Before**: 1,000명 x 12회/분 = **12,000 req/min**
**After**: 1,000명 x 2회/분 = **2,000 req/min**

---

## 추가 고려사항

### 오프라인 데이터 처리

Apple Watch가 일시적으로 네트워크에 연결되지 않을 경우를 대비한다.

```swift
class OfflineQueue {
    private var pendingRequests: [GameHistoryUpdateRequest] = []

    func enqueue(_ request: GameHistoryUpdateRequest) {
        pendingRequests.append(request)
        // 로컬 저장소에 백업
        UserDefaults.standard.set(pendingRequests, forKey: "pending_bio_data")
    }

    func flushOnReconnect() {
        // 재연결 시 배치로 전송
        apiClient.updateGameHistoryBatch(pendingRequests) { success in
            if success {
                self.pendingRequests.removeAll()
            }
        }
    }
}
```

### 완주 후 처리

완주 후에는 더 이상 폴링이 필요하지 않다. 클라이언트에서 `isDone` 플래그를 확인하여 폴링을 중단한다.

```swift
if response.isDone {
    stopPollingTimer()
    showCompletionScreen()
}
```

---

## 참고

- 기존 WebSocket 구현: `.claude/prompt/WATCH.md` 참조
- 경기 순위 규칙: `.claude/prompt/GAME_RANK_RULES.md` 참조
