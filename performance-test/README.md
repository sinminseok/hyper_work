# FunnyRun 성능 테스트 가이드

## 📋 개요

WebSocket vs Polling 방식의 성능을 비교하기 위한 테스트 환경

---

## 🛠️ 사전 준비

### 1. k6 설치

```bash
# macOS
brew install k6

# 설치 확인
k6 version
```

### 2. Docker 실행 확인

```bash
docker --version
docker-compose --version
```

---

## 🚀 실행 방법

### Step 1: 모니터링 스택 시작

```bash
cd performance-test
docker-compose -f docker-compose.monitoring.yml up -d

# 확인
docker ps
```

**접속 정보**
- Grafana: http://localhost:3000 (admin/admin)
- Prometheus: http://localhost:9090
- InfluxDB: http://localhost:8086 (admin/admin1234)

---

### Step 2: Spring Boot 애플리케이션 실행

```bash
# 프로젝트 루트에서
./gradlew :run_api:bootRun
```

또는

```bash
cd run_api
./gradlew bootRun
```

---

### Step 3: 테스트 데이터 준비

테스트를 위해 Redis에 더미 데이터를 추가해야 합니다.

```bash
# Redis에 접속
docker exec -it funnyrun-redis redis-cli

# 테스트 데이터 추가
ZADD game:rank:1 1 "1"
HSET game:data:1 "1" '{"rank":1,"currentDistance":2500,"targetDistance":5000,"currentBpm":150,"currentCadence":180,"done":false,"connectedWatch":true,"pollInterval":3}'
```

---

### Step 4: 성능 테스트 실행

#### 4-1. Polling 테스트 (작은 규모)

```bash
cd performance-test

# 100명 VU로 빠른 테스트
k6 run --vus 100 --duration 1m polling-test.js
```

#### 4-2. Polling 테스트 (전체 시나리오)

```bash
# InfluxDB에 결과 저장
k6 run polling-test.js \
  --out influxdb=http://localhost:8086 \
  -e API_URL=http://localhost:8080 \
  -e GAME_ID=1 \
  -e USER_ID=1
```

#### 4-3. WebSocket 테스트

```bash
k6 run websocket-test.js \
  --out influxdb=http://localhost:8086 \
  -e WS_URL=ws://localhost:8080/ws/game \
  -e GAME_ID=1 \
  -e USER_ID=1
```

---

## 📊 실시간 모니터링

### Terminal 1: k6 실행
```bash
k6 run polling-test.js
```

### Terminal 2: JVM 메모리 모니터링
```bash
watch -n 1 'curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | jq ".measurements[0].value / 1024 / 1024 | floor"'
```

### Terminal 3: Redis 모니터링
```bash
docker exec -it funnyrun-redis redis-cli --stat
```

### Terminal 4: 시스템 리소스
```bash
top -pid $(pgrep -f 'run_api')
```

---

## 📈 결과 분석

### k6 콘솔 출력 해석

```
checks.........................: 99.54% ✓ 48523    ✗ 224
http_req_duration..............: avg=45.32ms p(95)=156.23ms p(99)=298.45ms
http_reqs......................: 48523  80.87/s
```

**중요 지표**
- `http_req_duration p(99)`: P99 레이턴시
- `http_req_failed`: 에러율
- `http_reqs`: 초당 처리 요청 수 (RPS)

### Prometheus 쿼리

```promql
# JVM Heap 메모리 사용량
jvm_memory_used_bytes{area="heap"} / 1024 / 1024 / 1024

# HTTP 요청 처리율
rate(http_server_requests_seconds_count[1m])

# P95 응답 시간
histogram_quantile(0.95, http_server_requests_seconds_bucket)
```

---

## 🧹 정리

```bash
# 모니터링 스택 종료
docker-compose -f docker-compose.monitoring.yml down

# 볼륨까지 삭제 (데이터 초기화)
docker-compose -f docker-compose.monitoring.yml down -v
```

---

## 📝 테스트 시나리오

### Polling 테스트
| 단계 | 시간 | VU | 예상 RPS |
|------|------|-----|----------|
| Warm-up | 0~1분 | 0 → 100 | ~33 |
| Load | 1~3분 | 100 → 500 | ~167 |
| Peak | 3~5분 | 500 → 1,000 | ~333 |
| Sustain | 5~8분 | 1,000 (유지) | ~333 |
| Ramp-down | 8~9분 | 1,000 → 0 | ~0 |

### WebSocket 테스트
- 동일한 VU 시나리오
- 연결 유지 + 주기적 메시지 수신

---

## 🎯 예상 결과

| 지표 | WebSocket | Polling |
|------|-----------|---------|
| 메모리 사용량 | 5.2 GB | 1.8 GB |
| CPU 사용률 | 48% | 35% |
| P99 Latency | 45 ms | 3,200 ms |

---

## ⚠️ 문제 해결

### Redis 연결 실패
```bash
# Redis 컨테이너 상태 확인
docker ps | grep redis

# Redis 로그 확인
docker logs funnyrun-redis
```

### Spring Boot 시작 실패
```bash
# 포트 충돌 확인
lsof -i :8080

# 로그 확인
./gradlew :run_api:bootRun --info
```

### k6 테스트 실패
```bash
# 엔드포인트 확인
curl http://localhost:8080/v1/api/game-histories/status?gameId=1&userId=1

# WebSocket 확인
wscat -c ws://localhost:8080/ws/game
```
