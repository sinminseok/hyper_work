---
name: watch-polling
description: FunnyRun 프로젝트의 스마트워치 실시간 생체 데이터 수집 및 순위 시스템 가이드. HTTP Polling, Batch 전송, Redis 캐시, 순위 갱신 메커니즘을 다룸. "워치", "watch", "polling", "배치 전송", "batch", "생체 데이터", "순위 갱신", "Redis 캐시", "실시간 순위", "완주 처리", "Apple Watch", "Garmin" 관련 질문 시 사용.
---

# Watch Polling & Batch Transfer System

FunnyRun 프로젝트의 스마트워치(Apple Watch, Garmin) 실시간 생체 데이터 수집 및 순위 시스템 구현 가이드입니다.

## Quick Reference

### 핵심 API
- **배치 전송**: `PATCH /v1/api/game-histories/batch`
- **내 상태 조회**: `GET /v1/api/game-histories/status?gameId={id}&userId={id}`
- **1위 조회**: `GET /v1/api/game-histories/first-status?gameId={id}`

### 핵심 타이밍
- **배치 전송**: 30초마다 (6개 샘플)
- **Polling**: 3~7초 (진행률에 따라 동적)
- **순위 갱신**: 15초마다 (서버 Timer)

## Architecture Overview

### 배치 전송 플로우
```
워치 → PATCH /batch → GameHistoryService → Event 발행
  → GameHistoryBatchUpdateListener (Async)
  → 평균 계산 + 완주 체크 → MongoDB 저장 → Redis 캐시 업데이트
```

### 순위 갱신 플로우
```
GameScheduler (15초 Timer)
  → calculateRank() → MongoDB 조회 → 정렬 (완주자 우선)
  → 순위 부여 → MongoDB 저장 → Redis 캐시 전체 갱신
```

### Redis 캐시 구조
- `game:rank:{gameId}` - ZSet (score: rank, member: userId)
- `game:data:{gameId}` - Hash (userId: GameInProgressWatchResponse)

## Core Capabilities

### 1. 배치 데이터 처리
- 마지막 샘플의 거리만 사용
- BPM/Cadence: 배치 평균 → 기존 누적 평균과 병합
- 목표 거리 도달 시 자동으로 `isDone=true` 설정

### 2. 순위 정렬 기준
**SPEED**:
1. 완주자 우선 (`isDone=true`)
2. 소요 시간 짧은 순
3. 남은 거리 적은 순

**CADENCE/HEARTBEAT**:
1. 완주자 우선
2. 점수 작은 순 (목표값과 차이)
3. 소요 시간 짧은 순

### 3. Adaptive Polling
- 초반(0~30%): 7초
- 중반(30~90%): 5초
- 막판(90~100%): 3초
- 완주: -1 (중단)

### 4. 완주자 자동 승격
목표 거리 도달 → `isDone=true` 설정 → 다음 15초 갱신 시 완주자 그룹으로 자동 분류

## References

For comprehensive implementation details:
- **[references/ARCHITECTURE.md](references/ARCHITECTURE.md)** - 전체 아키텍처, API 상세, 구현 코드, 서버 플로우

## Key Files

- `GameHistoryController.java` - `/batch` 엔드포인트
- `GameHistoryBatchUpdateListener.java` - 비동기 배치 처리
- `GameScheduler.java` - 15초 타이머 순위 갱신
- `GameHistoryCacheService.java` - Redis 캐시 관리
- `SpeedRankService.java` - 정렬 로직
- `GameInProgressWatchResponse.java` - pollInterval 계산
