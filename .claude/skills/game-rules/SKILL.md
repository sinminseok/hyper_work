---
name: game-rules
description: FunnyRun 프로젝트의 경기 규칙 및 시스템 가이드. 경기 생성, 시작, 참가, 취소, 포기, 순위 산정, 상금 분배 규칙을 다룸. "경기 규칙", "게임 규칙", "경기 생성", "경기 시작", "참가비", "상금 분배", "순위 산정", "경기 취소", "경기 포기", "완주 조건", "GameScheduler" 관련 질문 시 사용.
---

# Game Rules & System

FunnyRun 프로젝트의 경기 생성, 시작, 관리, 순위 산정, 상금 분배 규칙 가이드입니다.

## Quick Reference

### 경기 기본 정보
- **참가비**: 2,500원 (쿠폰 차감)
- **총 상금**: 참가비 × 참가 인원
- **경기 시간**: 매일 5시~23시, 매시 정각
- **경기 타입**: SPEED, CADENCE, HEARTBEAT

### 상금 분배율
- 1위: 60%
- 2위: 15%
- 3위: 6%
- 4위: 3%
- 5~20위: 각 1%
- 21위 이하: 상금 없음

## Core Rules

### 1. 경기 생성 (Game Generation)

**자동 생성**
- 매일 새벽에 다음 날 경기 자동 생성
- GameType별 × GameDistance별 생성
- 5시~23시 매시 정각마다 경기 생성

**수동 생성** (테스트/관리)
- `POST /v1/api/games/test/init-games` - 일주일치 경기 생성
- `POST /v1/api/games/test/create-game` - 특정 날짜 경기 생성

**경기 정보**
- `name`: 경기 이름
- `type`: SPEED / CADENCE / HEARTBEAT
- `distance`: FIVE_KM / TEN_KM / HALF_MARATHON / FULL_MARATHON / etc.
- `startAt`: 경기 시작 시간
- `endAt`: 경기 종료 시간 (startAt + 1시간)
- `participatedCount`: 참가 인원
- `totalPrize`: 총 상금

### 2. 경기 참가 (Game Apply)

**참가 프로세스**
1. 쿠폰 2,500개 차감 (쿠폰 부족 시 에러)
2. GameHistory 생성
3. 참가 인원 증가 (`participatedCount++`)
4. 총 상금 자동 계산 및 상금 분배 금액 계산

**참가 제한**
- 경기 시작 10분 전까지 참가 가능
- 이미 시작된 경기는 참가 불가

**API**: `POST /v1/api/games/apply`

### 3. 경기 시작 (Game Start)

**자동 시작**
```
GameScheduler.startGames()
  - @Scheduled(cron = "0 0 5-23 * * *") // 매시 정각
  → 해당 시간 경기 조회
  → 각 경기마다 startGameRankLoop() 호출
  → Timer 생성 (15초마다 순위 갱신)
```

**순위 갱신 Loop**
- 15초마다 `calculateRank()` 호출
- 경기 종료 시간까지 반복
- 종료 시 Timer 취소 및 최종 결과 저장

**실제 참가 시작**
- API: `POST /v1/api/games/start`
- ConnectType 등록 (APPLE_WATCH / GARMIN / GALAXY_WATCH)
- startAt 기록 (실제 시작 시간)

### 4. 경기 취소 & 포기

**예정 경기 취소** (경기 시작 전)
- API: `DELETE /v1/api/games/{gameId}/withdraw`
- 처리:
  1. 참가 인원 감소 (`participatedCount--`)
  2. 쿠폰 환불 (`user.increaseCoupon()`)
  3. GameHistory 삭제

**진행 중 경기 포기** (경기 진행 중)
- API: `PATCH /v1/api/games/{gameId}/give-up`
- 처리:
  - GameHistory 삭제 (쿠폰 환불 없음)

### 5. 순위 산정 (Ranking)

**정렬 기준**

**SPEED (스피드 경기)**
```java
Comparator
  .comparing(GameHistory::isDone).reversed()  // 1. 완주자 우선
  .thenComparingLong(GameHistory::getDurationInSeconds)  // 2. 소요 시간
  .thenComparingDouble(GameHistory::getRemainingDistance)  // 3. 남은 거리
```

**CADENCE (케이던스 경기)**
```java
Comparator
  .comparing(GameHistory::isDone).reversed()  // 1. 완주자 우선
  .thenComparingDouble(GameHistory::getCadenceScore)  // 2. 케이던스 점수
  .thenComparingLong(GameHistory::getDurationInSeconds)  // 3. 소요 시간
```
- `cadenceScore = |targetCadence - currentCadence|`

**HEARTBEAT (심박수 경기)**
```java
Comparator
  .comparing(GameHistory::isDone).reversed()  // 1. 완주자 우선
  .thenComparingDouble(GameHistory::getHeartBeatScore)  // 2. 심박수 점수
  .thenComparingLong(GameHistory::getDurationInSeconds)  // 3. 소요 시간
```
- `heartBeatScore = |targetBpm - currentBpm|`

**완주 조건**
- `currentDistance >= gameDistance.getDistance()`
- 자동으로 `isDone=true`, `endAt=현재시간` 설정

### 6. 상금 분배 (Prize Distribution)

**상금 계산**
```java
GamePrizeCalculator:
  - 1위: totalPrize * 60%
  - 2위: totalPrize * 15%
  - 3위: totalPrize * 6%
  - 4위: totalPrize * 3%
  - 5~20위: totalPrize * 1% (각각)
```

**지급 프로세스** (경기 종료 시)
```
GameScheduler → 경기 종료 감지
  → GameFinishedEvent 발행
  → GameFinishedListener 처리:
      1. 1~20위 순위 조회 (rank 기준)
      2. 완주자만 필터링 (isDone=true)
      3. 상금 지급 (user.increasePoint)
      4. GameHistory에 prize 기록
      5. 1~3위 이름 Game에 기록
```

**지급 조건**
- 완주자만 상금 지급 (`isDone=true`)
- 20위까지만 상금 지급
- 순위가 0인 경우 제외 (순위 미정)

## Key Files

### 경기 생성 & 시작
- `GameScheduler.java` - 경기 자동 시작 및 순위 갱신
- `SpeedRankService.java` / `CadenceRankService.java` / `HeartBeatRankService.java` - 경기 타입별 정렬 로직
- `AbstractGameRankService.java` - 공통 순위 계산 로직

### 상금 분배
- `GamePrizeCalculator.java` - 상금 계산 유틸
- `GameFinishedListener.java` - 경기 종료 시 상금 지급

### 경기 참가 & 취소
- `GameService.java` - 참가, 취소, 포기 로직
- `Game.java` - 경기 엔티티 및 비즈니스 로직
