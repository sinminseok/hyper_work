# 스마트워치 실시간 생체 데이터 & 순위 시스템

## 아키텍처 개요
스마트워치(Apple Watch, Galaxy Watch, Garmin)에서 실시간 생체 데이터를 수집하고,
경기 참가자 간 실시간 순위를 계산하여 워치에 표시하는 시스템

---

## 📍 WebSocket 연결 정보

### 기본 설정

**엔드포인트**
- WebSocket 연결: `/game`
- 클라이언트 → 서버 전송: `/pub` prefix
- 서버 → 클라이언트 구독: `/sub` prefix

**환경별 URL**
- 개발: `ws://localhost:8080/game`
- 운영: `wss://your-domain/game` (HTTPS 필수)

### 주요 Destination

| 용도 | Destination | 설명 |
|------|-------------|------|
| **생체 데이터 전송** | `/pub/game/update` | 워치 → 서버 (5초마다) |
| **내 상태 구독** | `/sub/game/my/{gameId}/{userId}` | 내 순위, 거리, 심박수 등 수신 |
| **1위 정보 구독** | `/sub/game/first-place/{gameId}` | 현재 1위 실시간 정보 수신 |

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

### 2단계: WebSocket 연결

**연결 방식**
1. `/game` 엔드포인트로 WebSocket 연결
2. STOMP 프로토콜 사용
3. 연결 시 헤더에 `Authorization: Bearer {accessToken}` 포함

**중요 사항**
- 모든 경기가 하나의 `/game` 엔드포인트 공유
- Destination으로 개인별/경기별 라우팅 구분
- 연결 실패 시 토큰 재발급 후 재시도

---

### 3단계: 구독 설정

#### 3-1. 내 상태 구독 (필수)

**Destination**: `/sub/game/my/{gameId}/{userId}`

**수신 데이터**
- `rank`: 현재 순위 (1, 2, 3, ...)
- `currentDistance`: 현재 이동 거리 (m)
- `currentSpeed`: 현재 속도 (km/h)
- `currentBpm`: 현재 심박수
- `currentCadence`: 현재 케이던스
- `targetBpm`: 목표 심박수 (심박수 경기만 해당)
- `targetCadence`: 목표 케이던스 (케이던스 경기만 해당)
- `isDone`: 완주 여부
- `connectedWatch`: 워치 연결 상태

**수신 시점**
- 생체 데이터 전송 후 즉시 응답
- 15초마다 순위 갱신 시 자동 전송

#### 3-2. 1위 정보 구독 (선택)

**Destination**: `/sub/game/first-place/{gameId}`

**수신 데이터**
- 현재 1위의 순위, 거리, 속도, 심박수 등 모든 정보
- 내 정보와 동일한 구조

**수신 시점**
- 15초마다 순위 갱신 시 자동 전송
- 모든 참가자가 동일한 1위 정보 수신

**활용 예시**
- "1위와의 거리 차이" 표시
- "1위 페이스와 비교" 기능
- 실시간 리더보드 표시

---

### 4단계: 생체 데이터 전송

**Destination**: `/pub/game/update`

**전송 주기**: 5초마다

**전송 데이터**
```json
{
  "gameId": 123,
  "userId": 456,
  "currentBpm": 150.5,
  "currentCadence": 180.0,
  "currentDistance": 1250.5,
  "currentSpeed": 12.5,
  "currentFlightTime": 0.0,
  "currentGroundContactTime": 0.0,
  "currentPower": 0.0,
  "currentVerticalOscillation": 0.0
}
```

**필수 필드**
- `gameId`, `userId`: 경기 및 사용자 식별
- `currentBpm`: 심박수 (bpm)
- `currentCadence`: 케이던스 (spm, steps per minute)
- `currentDistance`: 누적 거리 (m)
- `currentSpeed`: 현재 속도 (km/h)

**선택 필드** (현재 미사용, 향후 확장용)
- `currentFlightTime`: 공중 체공 시간
- `currentGroundContactTime`: 지면 접촉 시간
- `currentPower`: 파워 (W)
- `currentVerticalOscillation`: 수직 진폭

---

### 5단계: 서버 응답 수신

**처리 흐름**
1. 워치 → 서버: 생체 데이터 전송 (`/pub/game/update`)
2. 서버: MongoDB에 데이터 저장 및 누적 평균 계산
3. 서버 → 워치: 업데이트된 내 상태 전송 (`/sub/game/my/{gameId}/{userId}`)
4. 워치: UI 업데이트 (순위, 거리, 심박수 등 표시)

**완주 처리**
- `currentDistance >= 목표거리` 도달 시
- 서버가 자동으로 `isDone: true` 설정
- 완주 시간(`endAt`) 기록
- 워치에서 완주 화면 표시

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
4. 구독 중인 모든 워치로 자동 전송
   - `/sub/game/my/{gameId}/{userId}` → 개인별 상태
   - `/sub/game/first-place/{gameId}` → 1위 정보

**실시간성**
- 생체 데이터 전송 시: 즉시 응답 (5초 주기)
- 순위 갱신 시: 15초마다 전체 재계산

---

## 📱 스마트워치별 구현 가이드

### 1. Galaxy Watch (Wear OS)

**개발 환경**
- Wear OS SDK (Android)
- Kotlin 권장
- STOMP 라이브러리: `com.github.NaikSoftware:StompProtocolAndroid`

**핵심 기능**
- **Foreground Service**: 백그라운드 연결 유지
- **타이머**: `Handler` 또는 `Timer`로 5초마다 데이터 전송


**연결 유지 전략**
- Foreground Service로 앱 강제 종료 방지
- 연결 끊김 시 자동 재연결 로직
- 배터리 최적화 예외 설정 안내

---

### 2. Apple Watch (watchOS)

**개발 환경**
- watchOS SDK
- Swift
- WebSocket 라이브러리: `Starscream` 또는 `SwiftStomp`

**핵심 기능**
- **HKWorkoutSession**: 운동 세션 백그라운드 실행
- **HealthKit**: 심박수, 거리, 케이던스 등 실시간 수집
- **타이머**: `Timer.scheduledTimer`로 5초마다 데이터 전송

**센서 매핑**
- 심박수: `HKQuantityType.quantityType(forIdentifier: .heartRate)`
- 거리: `HKQuantityType.quantityType(forIdentifier: .distanceWalkingRunning)`
- 케이던스: `HKQuantityType.quantityType(forIdentifier: .runningStrideLength)` + 계산

**연결 유지 전략**
- HKWorkoutSession으로 백그라운드 실행 권한 획득
- 화면 꺼짐 시에도 연결 유지
- Extended Runtime Session 활용

---

### 3. Garmin Watch

**개발 환경**
- Connect IQ SDK
- Monkey C 언어
- HTTP 또는 WebSocket 직접 구현

**핵심 기능**
- **Activity API**: 운동 세션 관리
- **Sensor API**: 심박수, 가속도계, GPS 데이터 수집
- **Communications API**: 서버 통신

**센서 매핑**
- 심박수: `Activity.getActivityInfo().currentHeartRate`
- 거리: `Activity.getActivityInfo().elapsedDistance`
- 케이던스: `Activity.getActivityInfo().currentCadence`

**연결 유지 전략**
- Activity 실행 중 백그라운드 통신 지원
- 배터리 최적화를 위해 GPS 정확도 조정
- 연결 끊김 시 로컬 데이터 큐잉 후 재전송

**제약 사항**
- WebSocket 지원이 제한적일 수 있음
- HTTP Long Polling 대안 고려 필요
- Connect IQ 버전별 API 차이 확인 필요

---

## 🎯 핵심 구현 포인트

### 1. 배터리 최적화

**데이터 전송 주기**
- 5초: 실시간성과 배터리의 균형점
- 너무 짧으면 배터리 소모 증가
- 너무 길면 순위 반영 지연

**센서 샘플링**
- 심박수: 1초마다 측정 → 5초 평균값 전송
- GPS: 최소 정확도로 설정 (10~20m)
- 불필요한 센서 비활성화

### 2. 연결 안정성

**재연결 로직**
- 연결 끊김 감지 시 자동 재연결
- 최대 3회 재시도 (지수 백오프)
- 재연결 실패 시 사용자 알림

**데이터 손실 방지**
- 오프라인 시 로컬 큐에 데이터 저장
- 재연결 시 누락된 데이터 일괄 전송
- 중복 전송 방지 (타임스탬프 체크)

### 3. 사용자 경험

**화면 표시 정보**
- **필수**: 순위, 현재 거리, 목표 거리까지 남은 거리
- **권장**: 현재 속도, 심박수, 예상 완주 시간
- **선택**: 1위와의 거리 차이, 페이스 비교

**완주 처리**
- `isDone: true` 수신 시 축하 화면 표시
- 최종 순위 및 기록 안내
- WebSocket 연결 유지 (경기 종료 시 까지)

**에러 핸들링**
- 네트워크 오류: 재연결 시도 안내
- 토큰 만료: 자동 갱신 시도
- 서버 오류: 관리자 문의 안내

---

## 🔧 개발 시 주의사항

### 인증 토큰 관리
- Access Token은 메모리에만 저장 (보안)
- Refresh Token은 안전한 저장소에 보관
- 토큰 만료 30초 전 자동 갱신

### WebSocket 연결
- 연결 전 네트워크 상태 확인
- STOMP 헤더에 반드시 토큰 포함
- 연결 실패 시 재시도 간격 증가 (1초 → 2초 → 4초)

### 데이터 전송
- JSON 직렬화 전 데이터 유효성 검증
- 음수 거리/속도 등 비정상 값 필터링
- 전송 실패 시 로컬 큐에 저장 후 재시도

### 순위 표시
- 순위 변동 시 애니메이션 효과 (선택)
- "계산 중" 상태 표시 (15초 갱신 주기 고려)
- 완주 후에도 순위 업데이트 지속 수신

---

## 📊 메시지 흐름도

```
[워치 앱]                          [서버]
   |                                 |
   |---(1) HTTP 토큰 발급----------->|
   |<------ Access/Refresh Token-----|
   |                                 |
   |---(2) WebSocket 연결: /game---->|
   |     (Authorization 헤더 포함)    |
   |<------ CONNECTED----------------|
   |                                 |
   |---(3) SUBSCRIBE---------------->|
   |     /sub/game/my/123/456        |
   |     /sub/game/first-place/123   |
   |                                 |
   |                                 |
   |--- 5초 경과 ------------------- |
   |                                 |
   |---(4) SEND-------------------->|
   |     /pub/game/update            |
   |     {bpm:150, distance:100,...} |
   |                                 |
   |                                 |-> MongoDB 저장
   |                                 |-> 누적 평균 계산
   |                                 |-> 완주 체크
   |                                 |
   |<---(5) MESSAGE------------------|
   |     /sub/game/my/123/456        |
   |     {rank:3, distance:100,...}  |
   |                                 |
   |--- UI 업데이트 (순위, 거리) ----|
   |                                 |
   |                                 |
   |--- 15초 경과 (순위 갱신) -------|
   |                                 |
   |                                 |-> 전체 순위 재계산
   |                                 |
   |<---(6) MESSAGE------------------|
   |     /sub/game/my/123/456        |
   |     {rank:2, distance:100,...}  | ← 순위 상승!
   |                                 |
   |<---(7) MESSAGE------------------|
   |     /sub/game/first-place/123   |
   |     {rank:1, distance:500,...}  | ← 1위 정보
   |                                 |
   |--- "1위와 400m 차이" 표시 ------|
   |                                 |
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

### 연결 순서
1. 토큰 발급 (HTTP)
2. WebSocket 연결 (/game)
3. 구독 설정 (내 상태, 1위 정보)
4. 생체 데이터 전송 (5초마다)
5. 서버 응답 수신 (즉시 + 15초마다)

### 주요 URL
- 토큰: `GET /v1/api/users/watch-connect-information/tokens?watchKey=xxx`
- WebSocket: `ws(s)://domain/game`
- 전송: `/pub/game/update`
- 구독: `/sub/game/my/{gameId}/{userId}`, `/sub/game/first-place/{gameId}`

### 데이터 주기
- 생체 데이터 전송: 5초마다
- 순위 갱신: 15초마다
- 토큰 갱신: Access Token 만료 30초 전

### 스마트워치 SDK
- Galaxy Watch: Wear OS + Kotlin
- Apple Watch: watchOS + Swift + HealthKit
- Garmin Watch: Connect IQ + Monkey C
