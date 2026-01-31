# 스마트워치 실시간 생체 데이터 & 순위 시스템

## 아키텍처 개요
스마트워치(Apple Watch, Galaxy Watch, Garmin)에서 실시간 생체 데이터를 수집하고,
경기 참가자 간 실시간 순위를 계산하여 워치에 표시하는 시스템

**통신 방식**: HTTP Polling (Adaptive Polling + Jitter)

---

## 📍 API 정보


### 주요 API

| 용도 | Method | Endpoint | 설명 |
|------|--------|----------|------|
| **생체 데이터 전송** | PATCH | `/v1/api/game-histories/batch` | 배치 데이터 전송 (3~5초마다) |
| **내 상태 조회** | GET | `/v1/api/game-histories/status` | 내 순위, 생체 데이터 조회 |
| **1위 정보 조회** | GET | `/v1/api/game-histories/first-status` | 현재 1위 정보 조회 |

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

### 2단계: 생체 데이터 배치 전송

**API**: `PATCH /v1/api/game-histories/batch`

**전송 주기**: 클라이언트는 생체 데이터를 5초 주기로 수집하고, 6번 모은 뒤 해당 API 로 한번에 보낸다.

**요청 데이터**
```json
{
  "gameId": 123,
  "userId": 456,
  "samples": [
    {
      "currentBpm": 150.5,
      "currentCadence": 180.0,
      "currentDistance": 1250.5,
      "currentSpeed": 12.5,
      "currentFlightTime": 0.0,
      "currentGroundContactTime": 0.0,
      "currentPower": 0.0,
      "currentVerticalOscillation": 0.0,
      "timestamp": "2024-01-15T10:30:00"
    },
    {
      "currentBpm": 152.0,
      "currentCadence": 182.0,
      "currentDistance": 1280.0,
      "...": "..."
    }
  ]
}
```

**필수 필드**
- `gameId`, `userId`: 경기 및 사용자 식별
- `samples`: 수집한 생체 데이터 배열

**Sample 필드**
- `currentBpm`: 심박수 (bpm)
- `currentCadence`: 케이던스 (spm)
- `currentDistance`: 누적 거리 (m)
- `currentSpeed`: 현재 속도 (km/h)

**선택 필드** (향후 확장용)
- `currentFlightTime`: 공중 체공 시간
- `currentGroundContactTime`: 지면 접촉 시간
- `currentPower`: 파워 (W)
- `currentVerticalOscillation`: 수직 진폭

---

### 3단계: 내 상태 조회 (Polling)

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
    "currentFlightTime": 0.0,
    "currentGroundContactTime": 0.0,
    "currentPower": 0.0,
    "currentVerticalOscillation": 0.0,
    "currentSpeed": 12.5,
    "done": false,
    "connectedWatch": true,
    "pollInterval": 3
  }
}
```

**주요 필드**
- `rank`: 현재 순위 (1, 2, 3, ...)
- `currentDistance`: 현재 이동 거리 (m)
- `targetDistance`: 목표 거리 (m)
- `done`: 완주 여부
- `pollInterval`: 다음 Polling 주기 (초)
  - `5`: 초반 (0~10%)
  - `3`: 중반 (10~90%)
  - `1`: 막판 (90~100%)
  - `-1`: 완주 (Polling 중단)

---

### 4단계: 1위 정보 조회 

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

경기 진행률에 따라 서버가 `pollInterval`을 동적으로 결정합니다.

| 진행률 | pollInterval | 이유 |
|--------|:------------:|------|
| 0~10% | 5초 | 초반, 순위 변동 적음 |
| 10~90% | 3초 | 일반 진행 |
| 90~100% | 1초 | 막판 스퍼트, 순위 변동 많음 |
| 완주 | -1 (중단) | 더 이상 조회 불필요 |

### Jitter (지터) 적용

모든 클라이언트가 같은 주기로 요청하면 트래픽이 몰립니다.
0~0.5초 랜덤 지연을 추가하여 요청을 분산합니다.

**클라이언트 구현 예시 (Swift)**
```swift
func scheduleNextPoll(pollInterval: Int) {
    if pollInterval < 0 { return }  // 완주 시 중단

    let jitter = Double.random(in: 0...0.5)
    let nextPoll = Double(pollInterval) + jitter

    DispatchQueue.main.asyncAfter(deadline: .now() + nextPoll) {
        self.fetchMyStatus()
    }
}
```

---

## ⏱️ 순위 갱신 시스템

### 자동 갱신 방식

**갱신 주기**: 15초마다

**갱신 시점**
- 경기 시작 후 15초마다 Timer로 자동 실행
- 경기 종료 시간까지 반복

**갱신 프로세스**
1. 서버가 모든 참가자의 GameHistory 조회
2. 경기 타입별 정렬 기준으로 순위 계산
   - **SPEED**: 완주자 우선 → 소요 시간 짧은 순 → 남은 거리 적은 순
   - **CADENCE**: 완주자 우선 → 케이던스 점수 작은 순 → 소요 시간 짧은 순
   - **HEARTBEAT**: 완주자 우선 → 심박수 점수 작은 순 → 소요 시간 짧은 순
3. 각 참가자의 `rank` 필드 업데이트
4. Redis 캐시 전체 갱신 (ZSet + Hash)

**실시간성**
- 생체 데이터 전송 시: 캐시에 즉시 반영
- 순위 갱신 시: 15초마다 전체 재계산

---

## 🗄️ Redis 캐시 구조

### 두 개의 Key 사용

순위 조회 성능을 위해 ZSet과 Hash를 조합하여 사용합니다.

**game:rank:{gameId} (ZSet)**
```
┌─────────────────────────────────┐
│  용도: 순위 조회/정렬            │
│                                 │
│  userId │ score (= rank)       │
│  ───────┼────────────────      │
│  "5"    │ 1  ← 1위             │
│  "3"    │ 2                    │
│  "7"    │ 3                    │
│                                 │
│  1위 조회: ZRANGE 0 0 → O(1)    │
│  내 순위: ZRANK → O(log N)      │
└─────────────────────────────────┘
```

**game:data:{gameId} (Hash)**
```
┌─────────────────────────────────┐
│  용도: 상세 데이터 조회          │
│                                 │
│  userId │ data (JSON)          │
│  ───────┼────────────────      │
│  "5"    │ {rank:1,             │
│         │  distance:4500,      │
│         │  bpm:152, ...}       │
│  "3"    │ {...}                │
│                                 │
│  상세 조회: HGET → O(1)         │
└─────────────────────────────────┘
```

### 시간 복잡도

| 연산 | Redis 명령어 | 시간 복잡도 |
|------|-------------|:----------:|
| 1위 조회 | `ZRANGE key 0 0` | O(1) |
| 내 순위 조회 | `ZRANK key member` | O(log N) |
| 상세 정보 조회 | `HGET key field` | O(1) |

---

## 📱 스마트워치별 구현 가이드

### 1. Apple Watch (watchOS)

**개발 환경**
- watchOS SDK
- Swift
- URLSession (HTTP 통신)

**핵심 기능**
- **HKWorkoutSession**: 운동 세션 백그라운드 실행
- **HealthKit**: 심박수, 거리, 케이던스 등 실시간 수집
- **Timer**: 주기적 데이터 전송 및 상태 조회

**센서 매핑**
- 심박수: `HKQuantityType.quantityType(forIdentifier: .heartRate)`
- 거리: `HKQuantityType.quantityType(forIdentifier: .distanceWalkingRunning)`
- 케이던스: `HKQuantityType.quantityType(forIdentifier: .runningStrideLength)` + 계산

**구현 예시**
```swift
class GamePollingManager {
    private var pollTimer: Timer?
    private var samples: [BioDataSample] = []

    // 생체 데이터 수집 (1초마다)
    func collectSample(bpm: Double, distance: Double, cadence: Double) {
        let sample = BioDataSample(
            currentBpm: bpm,
            currentDistance: distance,
            currentCadence: cadence,
            timestamp: Date()
        )
        samples.append(sample)
    }

    // 배치 전송 (3~5초마다)
    func sendBatch() {
        guard !samples.isEmpty else { return }

        let request = BatchUpdateRequest(
            gameId: gameId,
            userId: userId,
            samples: samples
        )

        api.sendBatch(request) { response in
            self.samples.removeAll()
        }
    }

    // 상태 조회 + 다음 Polling 스케줄
    func fetchMyStatus() {
        api.getStatus(gameId: gameId, userId: userId) { response in
            self.updateUI(response)
            self.scheduleNextPoll(pollInterval: response.pollInterval)
        }
    }

    // Jitter 적용 Polling
    func scheduleNextPoll(pollInterval: Int) {
        if pollInterval < 0 { return }

        let jitter = Double.random(in: 0...0.5)
        let delay = Double(pollInterval) + jitter

        pollTimer = Timer.scheduledTimer(withTimeInterval: delay, repeats: false) { _ in
            self.fetchMyStatus()
        }
    }
}
```

**백그라운드 실행**
- HKWorkoutSession으로 백그라운드 실행 권한 획득
- 화면 꺼짐 시에도 통신 유지
- Extended Runtime Session 활용

---

### 2. Galaxy Watch (Wear OS)

**개발 환경**
- Wear OS SDK (Android)
- Kotlin 권장
- OkHttp 또는 Retrofit (HTTP 통신)

**핵심 기능**
- **Foreground Service**: 백그라운드 실행 유지
- **SensorManager**: 심박수, 가속도계 데이터 수집
- **AlarmManager/Handler**: 주기적 작업 실행

**구현 예시**
```kotlin
class GamePollingService : Service() {
    private val samples = mutableListOf<BioDataSample>()
    private val handler = Handler(Looper.getMainLooper())

    private val pollRunnable = object : Runnable {
        override fun run() {
            fetchMyStatus()
        }
    }

    fun fetchMyStatus() {
        api.getStatus(gameId, userId).enqueue(object : Callback<StatusResponse> {
            override fun onResponse(response: StatusResponse) {
                updateUI(response)
                scheduleNextPoll(response.pollInterval)
            }
        })
    }

    fun scheduleNextPoll(pollInterval: Int) {
        if (pollInterval < 0) return

        val jitter = (0..500).random()  // 0~500ms
        val delay = pollInterval * 1000L + jitter

        handler.postDelayed(pollRunnable, delay)
    }
}
```

**연결 유지 전략**
- Foreground Service로 앱 강제 종료 방지
- 배터리 최적화 예외 설정 안내

---

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
- 3~5초마다 모아서 배치 전송
- 개별 전송 대비 연결 오버헤드 감소

**센서 샘플링**
- 심박수: 1초마다 측정 → 배치 전송
- GPS: 최소 정확도로 설정 (10~20m)
- 불필요한 센서 비활성화

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
- `done: true` 수신 시 축하 화면 표시
- `pollInterval: -1` 수신 시 Polling 중단
- 최종 순위 및 기록 안내

**에러 핸들링**
- 네트워크 오류: 재시도 안내
- 토큰 만료: 자동 갱신 시도
- 서버 오류: 관리자 문의 안내

---

## 📊 데이터 흐름도

```
[워치 앱]                              [서버]                    [Redis]
   |                                     |                          |
   |---(1) HTTP 토큰 발급--------------->|                          |
   |<------ Access/Refresh Token---------|                          |
   |                                     |                          |
   |                                     |                          |
   |--- 생체 데이터 수집 (1초마다) --------|                          |
   |                                     |                          |
   |--- 3초 경과 -------------------------|                          |
   |                                     |                          |
   |---(2) PATCH /batch----------------->|                          |
   |     {samples: [...]}                |                          |
   |                                     |-> MongoDB 저장            |
   |                                     |-> 캐시 갱신 ------------->|
   |<------ 200 OK ----------------------|                          |
   |                                     |                          |
   |                                     |                          |
   |---(3) GET /status------------------>|                          |
   |     ?gameId=123&userId=456          |                          |
   |                                     |<-- 캐시 조회 -------------|
   |<------ 응답 ------------------------|                          |
   |     {rank:3, pollInterval:3, ...}   |                          |
   |                                     |                          |
   |--- UI 업데이트 (순위, 거리) ---------|                          |
   |                                     |                          |
   |--- 3초 + jitter(0.3초) 후 ----------|                          |
   |                                     |                          |
   |---(4) GET /status------------------>|                          |
   |     ...                             |                          |
   |                                     |                          |
   |                                     |                          |
   |                             [15초마다 순위 계산]                 |
   |                                     |                          |
   |                                     |-> 전체 순위 재계산        |
   |                                     |-> 캐시 전체 갱신 -------->|
   |                                     |                          |
```

---

## 🧪 테스트용 API

**경기 강제 시작**
- API: `POST /v1/api/games/test/start/{gameId}`
- 스케줄러 대기 없이 즉시 경기 시작
- 15초마다 순위 갱신 시작
- 개발/테스트 환경 전용

**활용 방법**
1. 경기 생성 API로 테스트 경기 생성
2. 워치에서 경기 신청
3. 테스트 API로 즉시 경기 시작
4. 워치에서 데이터 전송 및 순위 확인

---

## 📝 요약

### 통신 순서
1. 토큰 발급 (HTTP GET)
2. 생체 데이터 배치 전송 (HTTP PATCH, 3~5초마다)
3. 내 상태 조회 (HTTP GET, pollInterval에 따라)
4. 1위 정보 조회 (HTTP GET, 선택)

### 주요 API
| 용도 | API |
|------|-----|
| 토큰 발급 | `GET /v1/api/users/watch-connect-information/tokens?watchKey=xxx` |
| 배치 전송 | `PATCH /v1/api/game-histories/batch` |
| 내 상태 | `GET /v1/api/game-histories/status?gameId=xxx&userId=xxx` |
| 1위 정보 | `GET /v1/api/game-histories/first-status?gameId=xxx` |

### 데이터 주기
- 생체 데이터 수집: 1초마다
- 배치 전송: 3~5초마다
- 상태 조회: pollInterval (1~5초, 동적)
- 순위 갱신 (서버): 15초마다
- 토큰 갱신: Access Token 만료 30초 전

### Polling 최적화
- **Adaptive Polling**: 경기 진행률에 따라 주기 조절
- **Jitter**: 0~0.5초 랜덤 지연으로 트래픽 분산
- **Redis 캐시**: ZSet + Hash로 O(log N) 조회

### 스마트워치 SDK
- Apple Watch: watchOS + Swift + URLSession
- Galaxy Watch: Wear OS + Kotlin + OkHttp
- Garmin Watch: Connect IQ + Monkey C
