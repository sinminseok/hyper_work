# 스마트워치 실시간 생체 데이터 & 순위 시스템

## 아키텍처 개요
스마트워치(Apple Watch, Galaxy Watch, Garmin)에서 실시간 생체 데이터를 수집하고,
경기 참가자 간 실시간 순위를 계산하여 워치에 표시하는 시스템

**통신 방식**: HTTP Polling (Adaptive Polling + Jitter)

---

## 📍 API 정보


### 주요 API

| 용도            | Method | Endpoint                             | 설명 |
|---------------|--------|--------------------------------------|------|
| **자신의 경기 조회** | GET    | `/v1/api/games/participate/history`                 | 진행중/참가예정 경기 내역 조회 |
| **생체 데이터 전송** | PATCH  | `/v1/api/game-histories/batch`       | 배치 데이터 전송 (3~5초마다) |
| **내 상태 조회**   | GET    | `/v1/api/game-histories/status`      | 내 순위, 생체 데이터 조회 |
| **1위 정보 조회**  | GET    | `/v1/api/game-histories/first-status` | 현재 1위 정보 조회 |

---

## 🔄 전체 흐름

### 1단계: 인증 토큰 발급

**API**: `GET /v1/api/users/watch-connect-information/tokens?watchKey={워치키}`

**목적**
- 워치 전용 Access Token 발급 (유효기간: 1시간)
- Refresh Token 발급 (유효기간: 2주)
- 워치키는 사전에 모바일 앱에서 생성하여 워치로 전달

**응답**
```json
{
  "success": true,
  "message": "워치 연결 성공",
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc..."
  }
}
```

**토큰 재발급**
- API: `POST /v1/api/auth/refresh`
- Access Token 만료 시 Refresh Token으로 자동 갱신

---

### 2단계: 자신의 경기 내역 조회

**API**: `GET /v1/api/games/participate/history`

**목적**
- 현재 진행중이거나 참가 예정인 자신의 경기 목록을 조회
- 인증 토큰 발급 후, 워치에서 표시할 경기 목록을 가져올 때 사용
- **이미 종료된 경기는 조회되지 않음** (진행중 또는 시작 전 경기만 반환)

**요청**
- Method: `GET`
- Header: `Authorization: Bearer {accessToken}`
- 별도의 요청 파라미터 없음 (로그인된 사용자 정보를 토큰에서 추출)

**응답 데이터**
- `List<GameHistoryResponse>`
```json
{
  "success": true,
  "message": "참가중 or 참가 예정인 경기 기록 조회",
  "data": [
    {
      "gameDate": "2024-01-15",
      "gameId": 123,
      "gameType": "SPEED",
      "gameDistance": "FIVE_KM",
      "startAt": "2024-01-15T10:00:00",
      "endAt": "2024-01-15T11:00:00",
      "myRank": 3,
      "participatedCount": 50,
      "targetBpm": 150.0,
      "targetCadence": 180.0,
      "currentBpm": 152.3,
      "currentCadence": 178.5,
      "currentDistance": 2500.0,
      "myPrize": 0.0,
      "connectedWatch": true,
      "isDone": false
    }
  ]
}
```

**주요 필드**
- `gameDate`: 경기 날짜
- `gameId`: 경기 ID (이후 API 호출 시 사용)
- `gameType`: 경기 타입 (`SPEED`, `HEARTBEAT`, `CADENCE`)
- `gameDistance`: 경기 목표 거리
- `startAt` / `endAt`: 경기 시작/종료 시간
- `myRank`: 현재 내 순위
- `participatedCount`: 총 참가자 수
- `targetBpm` / `targetCadence`: 목표 심박수 / 케이던스
- `currentBpm` / `currentCadence`: 현재 심박수 / 케이던스
- `currentDistance`: 현재 누적 이동 거리 (m)
- `myPrize`: 현재 상금
- `connectedWatch`: 워치 연결 여부
- `isDone`: 완주 여부

**활용**
- 워치에서 참가 가능한 경기 목록을 화면에 표시
- `gameId`를 획득하여 이후 생체 데이터 전송, 상태 조회 등의 API 호출에 사용

---

### 3단계: 생체 데이터 배치 전송

**API**: `PATCH /v1/api/game-histories/batch` 또는 `POST /v1/api/game-histories/batch` (Garmin용)

**전송 주기**: 클라이언트는 생체 데이터를 5초 주기로 수집하고, 6번 모은 뒤 해당 API로 한번에 보낸다.

**요청 데이터**
```json
{
  "gameId": 123,
  "userId": 456,
  "samples": [
    {
      "currentBpm": 150.5,
      "currentCadence": 180.0,
      "currentDistance": 1250.5
    },
    {
      "currentBpm": 152.0,
      "currentCadence": 182.0,
      "currentDistance": 1280.0
    }
  ]
}
```

**필수 필드**
- `gameId`, `userId`: 경기 및 사용자 식별
- `samples`: 수집한 생체 데이터 배열 (3~6개 권장)

**Sample 필드**
- `currentBpm`: 심박수 (bpm) - Double, 0이면 무시
- `currentCadence`: 케이던스 (spm) - double, 0이면 무시
- `currentDistance`: 누적 거리 (m) - double, 마지막 샘플의 값만 사용

**서버 처리 플로우**
1. `GameHistoryService.updateBatchGameHistory()` 호출 → `GameHistoryBatchUpdateEvent` 발행
2. `GameHistoryBatchUpdateListener` (비동기 + 트랜잭션) 처리:
   - ✅ **워치 연결 처리**: `isConnectedWatch=false`면 자동으로 `true` 설정
   - ✅ **거리 업데이트**: 배치의 마지막 샘플의 `currentDistance` 사용
   - ✅ **평균 계산**: 기존 누적 평균과 배치 평균을 병합
     - BPM: 유효한 샘플들의 평균 계산 후 기존 평균과 병합
     - Cadence: 유효한 샘플들의 평균 계산 후 기존 평균과 병합
   - ✅ **완주 체크**: `currentDistance >= targetDistance`면 자동으로 `isDone=true`, `endAt=현재시간` 설정
   - ✅ **MongoDB 저장**: GameHistory 업데이트
   - ✅ **Redis 캐시 업데이트**: 실시간 순위 반영

**평균 계산 로직**
```java
// 기존 누적 평균 (existingAvg * existingCount) + 배치 평균 (batchAvg * batchCount)
// ─────────────────────────────────────────────────────────────────────────
//                    existingCount + batchCount

// 예시: 기존 BPM 평균 150 (10회) + 배치 BPM 평균 155 (3회)
// = (150 * 10 + 155 * 3) / 13 = 151.15
```

**자동 완주 처리**
- 목표 거리 도달 시 `GameHistory.checkDoneByDistance()` 자동 호출
- `isDone=true`, `endAt=현재시간` 설정
- 다음 15초 순위 갱신 시 완주자 그룹으로 분류되어 상위 순위로 자동 배치


---

### 4단계: 내 상태 조회 (Polling)

**API**: `GET /v1/api/game-histories/status?gameId={gameId}&userId={userId}`

**조회 주기**: 응답의 `pollInterval` 값에 따라 동적 조절

**응답 데이터**
- - GameInProgressWatchResponse
```json
{
  "success": true,
  "message": "현재 내 등수 조회 성공",
  "data": {
    "rank": 3,
    "targetBpm": 150,
    "targetCadence": 180,
    "currentBpm": 152.3,
    "currentCadence": 178.5,
    "currentDistance": 4500.0,
    "targetDistance": 5000.0,
    "done": false,
    "connectedWatch": true,
    "pollInterval": 3
  }
}
```

**주요 필드**
- `rank`: 현재 순위 (1, 2, 3, ...)
- `targetBpm`: 목표 심박수 (bpm)
- `targetCadence`: 목표 케이던스 (spm)
- `currentBpm`: 현재 심박수 (bpm)
- `currentCadence`: 현재 케이던스 (spm)
- `currentDistance`: 현재 이동 거리 (m)
- `targetDistance`: 목표 거리 (m)
- `done`: 완주 여부
- `connectedWatch`: 워치 연결 여부
- `pollInterval`: 다음 Polling 주기 (초)
  - `7초`: 초반 (0~30%)
  - `5초`: 중반 (30~90%)
  - `3초`: 막판 (90~100%)
  - `-1`: 완주 (Polling 중단)

---

### 5단계: 1위 정보 조회

**API**: `GET /v1/api/game-histories/first-status?gameId={gameId}`

**응답 데이터**
- GameInProgressWatchResponse
- 현재 1위의 순위, 거리, 속도, 심박수 등 모든 정보
- 내 정보와 동일한 구조

**활용 예시**
- "1위와의 거리 차이" 표시
- "1위 페이스와 비교" 기능

---

## ⏱️ Adaptive Polling (가변 주기)

### Polling 주기 결정

경기 진행률(`currentDistance / targetDistance`)에 따라 서버가 `pollInterval`을 동적으로 결정합니다.

**구현 로직** (`GameInProgressWatchResponse.calculatePollInterval()`)
```java
if (isDone) {
    return -1;  // 완주 → Polling 중단
}

double progressRatio = currentDistance / targetDistance;

if (progressRatio >= 0.9) {
    return 3;   // 90% 이상 → 막판 스퍼트
} else if (progressRatio <= 0.3) {
    return 7;   // 30% 이하 → 초반
} else {
    return 5;   // 중반
}
```

| 진행률 | pollInterval | 이유 |
|--------|:------------:|------|
| 0~30% | 7초 | 초반, 순위 변동 적음, 배터리 절약 |
| 30~90% | 5초 | 일반 진행 |
| 90~100% | 3초 | 막판 스퍼트, 순위 변동 많음 |
| 완주 (`isDone=true`) | -1 (중단) | 더 이상 조회 불필요 |

### Jitter (지터) 적용

모든 클라이언트가 같은 주기로 요청하면 트래픽이 몰립니다.
0~0.5초 랜덤 지연을 추가하여 요청을 분산합니다.

---

## ⏱️ 순위 갱신 시스템

### 자동 갱신 방식

**갱신 주기**: 15초마다 (Java Timer 사용)

**갱신 시점**
- 경기 시작 시각에 `GameScheduler.startGameRankLoop()` 호출
- Timer를 생성하여 15초마다 `TimerTask` 실행
- 경기 종료 시간(`game.endAt`)까지 반복
- 경기 종료 시 Timer 취소 및 최종 결과 저장

**갱신 프로세스** (`AbstractGameRankService.calculateRank()`)
1. **DB 조회**: MongoDB에서 해당 경기의 모든 `GameHistory` 조회
2. **정렬**: 경기 타입별 정렬 기준 적용
3. **순위 부여**: 1등부터 순차적으로 `rank` 필드 설정
4. **DB 저장**: 모든 GameHistory의 rank 값 업데이트
5. **Redis 캐시 갱신**: 각 참가자의 순위 및 상태를 Redis ZSet + Hash에 저장

### 경기 타입별 정렬 기준

#### SPEED (스피드 경기)
```java
Comparator
    .comparing(GameHistory::isDone).reversed()         // 1순위: 완주자 우선 (true가 먼저)
    .thenComparingLong(GameHistory::getDurationInSeconds)  // 2순위: 소요 시간 짧은 순
    .thenComparingDouble(GameHistory::getRemainingDistance)  // 3순위: 남은 거리 적은 순
```

**완주자 vs 미완주자**
- **완주자** (`isDone=true`): `getDurationInSeconds()` → 실제 소요 시간 (초) 반환
  - 예: 10분 완주 → 600초
- **미완주자** (`isDone=false`): `getDurationInSeconds()` → `Long.MAX_VALUE` 반환
  - 자동으로 완주자보다 하위 순위로 배치
  - 미완주자끼리는 남은 거리(`remainingDistance`) 기준 정렬

**완주 판정**
- `currentDistance >= gameDistance.getDistance()` → 자동으로 `isDone=true` 설정
- `endAt = LocalDateTime.now()` 기록 (소요 시간 계산용)

#### CADENCE (케이던스 경기)
```java
Comparator
    .comparing(GameHistory::isDone).reversed()         // 1순위: 완주자 우선
    .thenComparingDouble(GameHistory::getCadenceScore)    // 2순위: 케이던스 점수 작은 순
    .thenComparingLong(GameHistory::getDurationInSeconds)  // 3순위: 소요 시간 짧은 순
```
- `cadenceScore = |targetCadence - currentCadence|` (절댓값 차이)

#### HEARTBEAT (심박수 경기)
```java
Comparator
    .comparing(GameHistory::isDone).reversed()         // 1순위: 완주자 우선
    .thenComparingDouble(GameHistory::getHeartBeatScore)  // 2순위: 심박수 점수 작은 순
    .thenComparingLong(GameHistory::getDurationInSeconds)  // 3순위: 소요 시간 짧은 순
```
- `heartBeatScore = |targetBpm - currentBpm|` (절댓값 차이)

### Redis 캐시 구조

**두 가지 키 사용** (경기당 2개)

1. **`game:rank:{gameId}`** - ZSet (순위 조회용)
   - **Score**: `rank` 값 (1, 2, 3, ...) - 낮을수록 높은 순위
   - **Member**: `userId` (문자열)
   - **용도**: 1위 조회, 순위별 조회

2. **`game:data:{gameId}`** - Hash (상세 정보 조회용)
   - **Field**: `userId` (문자열)
   - **Value**: `GameInProgressWatchResponse` JSON
   - **용도**: 사용자별 상세 정보 조회

**캐시 갱신 시점**
1. **배치 데이터 전송 시** (즉시 반영)
   - `GameHistoryBatchUpdateListener` → `gameHistoryCacheService.updateUserStatusCache()`
   - 해당 사용자의 데이터만 업데이트 (순위는 기존 값 유지)
2. **15초마다 순위 갱신 시** (전체 재계산)
   - `GameScheduler` → `calculateRank()` → 모든 참가자의 순위 + 데이터 업데이트

**TTL 설정**: 2시간 (경기 종료 후 자동 삭제)

**실시간성**
- 생체 데이터 전송 시: 캐시에 즉시 반영 (순위는 기존 값)
- 순위 갱신 시: 15초마다 전체 재계산 후 반영
- 완주 후: 다음 15초 갱신 시 자동으로 상위 순위로 재배치

---


### Redis 캐시 조회 성능

| 연산 | 구현 방식 | 시간 복잡도 |
|------|----------|:----------:|
| 1위 조회 | `ZRANGE game:rank:{gameId} 0 0` → `HGET game:data:{gameId} {userId}` | O(log 1) + O(1) ≈ **O(1)** |
| 내 순위 조회 | `HGET game:data:{gameId} {userId}` | **O(1)** |
| 상세 정보 조회 | `HGET game:data:{gameId} {userId}` | **O(1)** |
| 순위 갱신 (전체) | MongoDB 조회 + 정렬 + Redis 업데이트 | **O(N log N)** |

**캐시 히트 시 응답 시간**: ~5ms 이하
**캐시 미스 시 응답 시간**: ~50ms (MongoDB 조회 포함)

---

## 📱 스마트워치별 구현 가이드


### 3. Garmin Watch

**개발 환경**
- Connect IQ SDK
- Monkey C 언어
- Communications API (HTTP 통신)

**핵심 기능**
- **Activity API**: 운동 세션 관리
- **Sensor API**: 심박수, 가속도계, GPS 데이터 수집
- **Communications API**: HTTP 통신

**센서 매핑**
- 심박수: `Activity.getActivityInfo().currentHeartRate`
- 거리: `Activity.getActivityInfo().elapsedDistance`
- 케이던스: `Activity.getActivityInfo().currentCadence`

**연결 유지 전략**
- Activity 실행 중 백그라운드 통신 지원
- 배터리 최적화를 위해 GPS 정확도 조정
- 연결 끊김 시 로컬 데이터 큐잉 후 재전송

---

## 🎯 핵심 구현 포인트

### 1. 배터리 최적화

**배치 전송**
- 생체 데이터를 1초마다 수집
- 3~6개 샘플을 모아서 배치 전송 (30초마다)
- 개별 전송 대비 HTTP 연결 오버헤드 80% 감소

**배치 전송 상세**
```json
// 6개 샘플 수집 후 한 번에 전송
{
  "gameId": 123,
  "userId": 456,
  "samples": [
    {"currentBpm": 150, "currentCadence": 180, "currentDistance": 1000},
    {"currentBpm": 152, "currentCadence": 181, "currentDistance": 1005},
    {"currentBpm": 151, "currentCadence": 180, "currentDistance": 1010},
    {"currentBpm": 153, "currentCadence": 182, "currentDistance": 1015},
    {"currentBpm": 152, "currentCadence": 181, "currentDistance": 1020},
    {"currentBpm": 151, "currentCadence": 180, "currentDistance": 1025}
  ]
}

// 서버 처리:
// - currentDistance: 마지막 샘플 값(1025m) 사용
// - currentBpm: 6개 샘플 평균(151.5) → 기존 누적 평균과 병합
// - currentCadence: 6개 샘플 평균(180.67) → 기존 누적 평균과 병합
```

**센서 샘플링**
- 심박수: 1초마다 측정 → 배치 전송
- 케이던스: 1초마다 측정 → 배치 전송
- GPS 거리: 1초마다 누적 → 마지막 값만 전송
- 불필요한 센서 비활성화 (자이로스코프, 기압계 등)

### 2. 네트워크 안정성

**재시도 로직**
- 요청 실패 시 최대 3회 재시도 (지수 백오프)
- 재시도 실패 시 사용자 알림

**데이터 손실 방지**
- 오프라인 시 로컬 큐에 데이터 저장
- 재연결 시 누락된 데이터 일괄 전송

### 3. 사용자 경험

**화면 표시 정보**
- **필수**: 순위, 현재 거리, 목표 거리까지 남은 거리
- **권장**: 현재 속도, 심박수, 예상 완주 시간
- **선택**: 1위와의 거리 차이, 페이스 비교

**완주 처리**
- 서버는 목표 거리 도달 시 자동으로 `isDone=true` 설정
- 클라이언트는 Polling 응답의 `done: true` 수신 시:
  - 축하 화면 표시
  - 배치 데이터 전송 중단
  - Polling 중단 (`pollInterval: -1` 수신)
- 완주 후 다음 15초 순위 갱신 시:
  - 자동으로 완주자 그룹으로 분류
  - 소요 시간 기준으로 순위 재배치
  - 최종 순위 확정

**에러 핸들링**
- 네트워크 오류: 재시도 안내
- 토큰 만료: 자동 갱신 시도
- 서버 오류: 관리자 문의 안내


## 요구 사항
- 경기는 동시에 시작될 수 있다.
  - 심박수, 케이던스는 생체 데이터를 공유해서 사용 가능하지만 "이동 거리" 는 각 경기마다 독립적으로 수집해 누적 거리를 계산해야 한다.
- 일시 정지 기능은 각 경기마다 따로 관리해야 한다.
  - 일시 정지 후 다시 재생할때, 이전까지 누적된 이동 거리를 이어서 수집해 서버로 전송해야 한다.
- 백그라운드에서 동작
  - polling 은 백그라운드에서 동작하지 않아도 되지만, 생체 데이터 수집과 서버로 batch 전송은 백그라운드에서도 계속 실행되어야 한다.

## 🔧 서버 아키텍처 상세

### 배치 데이터 처리 플로우

```
클라이언트 (워치)
    ↓ PATCH /v1/api/game-histories/batch
GameHistoryController.updateGameHistoryBatch()
    ↓
GameHistoryService.updateBatchGameHistory()
    ↓ publishEvent(GameHistoryBatchUpdateEvent)
GameHistoryBatchUpdateListener.handleBatchUpdate()  [@Async, @Transactional(REQUIRES_NEW)]
    ↓
    ├─ 1. GameHistory 조회 (MongoDB)
    ├─ 2. 워치 연결 처리 (isConnectedWatch = true)
    ├─ 3. GameHistory.updateFromBatch()
    │     ├─ 거리: 마지막 샘플 값 사용
    │     ├─ BPM: 배치 평균 → 기존 누적 평균과 병합
    │     └─ Cadence: 배치 평균 → 기존 누적 평균과 병합
    ├─ 4. GameHistory.checkDoneByDistance()
    │     └─ currentDistance >= targetDistance → isDone=true, endAt=now
    ├─ 5. MongoDB 저장
    └─ 6. Redis 캐시 업데이트 (즉시 반영)
```

### 순위 갱신 플로우

```
GameScheduler.startGameRankLoop()  [경기 시작 시 호출]
    ↓
Timer.scheduleAtFixedRate(task, 0, 15000)  [15초마다]
    ↓
GameRankService.calculateRank()
    ↓
    ├─ 1. MongoDB에서 모든 GameHistory 조회
    ├─ 2. 경기 타입별 정렬 (SPEED/CADENCE/HEARTBEAT)
    │     └─ 완주자 우선 → 점수/시간 순 정렬
    ├─ 3. 순위 부여 (1, 2, 3, ...)
    ├─ 4. MongoDB에 rank 저장
    └─ 5. Redis 캐시 전체 갱신
          ├─ game:rank:{gameId} (ZSet) ← score: rank, member: userId
          └─ game:data:{gameId} (Hash) ← userId: GameInProgressWatchResponse
```

### 완주자 자동 승격 메커니즘

```
사용자 A가 목표 거리 도달
    ↓
배치 전송 → checkDoneByDistance() → isDone=true, endAt=now 설정
    ↓
MongoDB 저장 (isDone=true, endAt 기록)
    ↓
Redis 캐시 업데이트 (기존 rank 유지)
    ↓
[최대 15초 대기]
    ↓
다음 순위 갱신 실행
    ↓
정렬 시 isDone=true가 우선순위
    ↓
getDurationInSeconds() 계산 (endAt - startAt)
    ↓
완주자 그룹에서 소요 시간 순으로 재배치
    ↓
새로운 rank 부여 및 캐시 갱신
    ↓
클라이언트 Polling 응답에 새 순위 반영
```

---

## 📝 요약

### 통신 순서
1. 토큰 발급 (HTTP GET)
2. 자신의 경기 내역 조회 (HTTP GET)
3. 생체 데이터 배치 전송 (HTTP PATCH/POST, 30초마다 6개 샘플)
4. 내 상태 조회 (HTTP GET, pollInterval에 따라 1~5초)
5. 1위 정보 조회 (HTTP GET, 선택)

### 주요 API
| 용도 | Method | Endpoint |
|------|--------|----------|
| 토큰 발급 | GET | `/v1/api/users/watch-connect-information/tokens?watchKey=xxx` |
| 경기 내역 조회 | GET | `/v1/api/games/participate/history` |
| 배치 전송 | PATCH/POST | `/v1/api/game-histories/batch` |
| 내 상태 | GET | `/v1/api/game-histories/status?gameId=xxx&userId=xxx` |
| 1위 정보 | GET | `/v1/api/game-histories/first-status?gameId=xxx` |

### 데이터 주기 및 타이밍
| 항목 | 주기 | 비고 |
|------|------|------|
| 생체 데이터 수집 | 1초마다 | 로컬 배열에 저장 |
| 배치 전송 | 30초마다 | 6개 샘플 모아서 전송 |
| 상태 조회 (Polling) | 3~7초 동적 | pollInterval 응답 값 기준 (진행률에 따라) |
| 순위 갱신 (서버) | 15초마다 | Timer 자동 실행 (경기 종료까지) |
| 토큰 갱신 | 만료 30초 전 | Refresh Token 사용 |
| 캐시 TTL | 2시간 | 경기 종료 후 자동 삭제 |

### Polling 최적화
- **Adaptive Polling**: 경기 진행률에 따라 주기 조절 (7초 → 5초 → 3초)
  - 초반(0~30%): 7초 - 배터리 절약
  - 중반(30~90%): 5초 - 일반 진행
  - 막판(90~100%): 3초 - 실시간 순위 변동
- **Jitter**: 0~0.5초 랜덤 지연으로 트래픽 분산
- **Redis 캐시**: ZSet + Hash로 O(1) 조회
- **배치 전송**: 샘플 6개 모아서 전송 → HTTP 연결 오버헤드 80% 감소

### 스마트워치 SDK
- Apple Watch: watchOS + Swift + URLSession
- Galaxy Watch: Wear OS + Kotlin + OkHttp
- Garmin Watch: Connect IQ + Monkey C

### 핵심 성능 지표
- 배치 전송 응답 시간: ~100ms 이하 (비동기 처리)
- Polling 응답 시간: ~5ms (캐시 히트 시)
- 순위 갱신 주기: 15초 (Timer)
- 완주 후 순위 반영: 최대 15초 이내
