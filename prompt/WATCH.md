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
- `currentDistance`: 현재 이동 거리 (m)
- `targetDistance`: 목표 거리 (m)
- `done`: 완주 여부
- `pollInterval`: 다음 Polling 주기 (초)
  - `5`: 초반 (0~10%)
  - `3`: 중반 (10~90%)
  - `1`: 막판 (90~100%)
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


### 시간 복잡도

| 연산 | Redis 명령어 | 시간 복잡도 |
|------|-------------|:----------:|
| 1위 조회 | `ZRANGE key 0 0` | O(1) |
| 내 순위 조회 | `ZRANK key member` | O(log N) |
| 상세 정보 조회 | `HGET key field` | O(1) |

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


## 요구 사항
- 경기는 동시에 시작될 수 있다.
  - 심박수, 케이던스는 생체 데이터를 공유해서 사용 가능하지만 "이동 거리" 는 각 경기마다 독립적으로 수집해 누적 거리를 계산해야 한다.
- 일시 정지 기능은 각 경기마다 따로 관리해야 한다.
  - 일시 정지 후 다시 재생할때, 이전까지 누적된 이동 거리를 이어서 수집해 서버로 전송해야 한다.
- 백그라운드에서 동작
  - polling 은 백그라운드에서 동작하지 않아도 되지만, 생체 데이터 수집과 서버로 batch 전송은 백그라운드에서도 계속 실행되어야 한다.

## 📝 요약

### 통신 순서
1. 토큰 발급 (HTTP GET)
2. 자신의 경기 내역 조회 (HTTP GET)
3. 생체 데이터 배치 전송 (HTTP PATCH, 3~5초마다)
4. 내 상태 조회 (HTTP GET, pollInterval에 따라)
5. 1위 정보 조회 (HTTP GET, 선택)

### 주요 API
| 용도 | API |
|------|-----|
| 토큰 발급 | `GET /v1/api/users/watch-connect-information/tokens?watchKey=xxx` |
| 경기 내역 조회 | `GET /v1/api/games/participate/history` |
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
