## Funny Run - (하이퍼브레인랩 외주)

----
```
사용자는 달리기 경기에 참여해 다른 참가자들과 순위를 겨룹니다.
경기 종료 후, 순위권에 오르면 리워드를 지급받으며, 이를 현금화해 수익을 얻을 수 있습니다.
게임 진행 과정에서는 심박수, 속도, 이동거리, 케이던스, 수직 진폭 등 다양한 생체 데이터를 수집해, 
보다 정밀하게 순위를 산정합니다.
```

### Version

---

Backend : `Spring Boot`, `JPA`, `WebSocket`  
Database : `MongoDB`, `MySQL`  
Devops : `Jenkis`, `Docker`, `AWS`    
Frontend : `TypeScript`, `Flutter`

### TODO
- [ ] MongoDB 계정 변경 

### 시스템 아키텍처  

### 모듈 구조  

### AWS 사용 가이드

## 📋 목차
- [현재 AWS 구성](#현재-aws-구성)
- [SNS/SQS 아키텍처](#snssqs-아키텍처)
- [네이밍 규칙](#네이밍-규칙)
- [새로운 SNS/SQS 추가 가이드](#새로운-snssqs-추가-가이드)
- [환경 변수 설정](#환경-변수-설정)

---

## 현재 AWS 구성

### 사용 중인 SNS Topics

| 환경 | Topic 이름 | 용도 |
|------|-----------|------|
| DEV | `DEV-payment-created-topic` | 결제 완료 이벤트 발행 |
| DEV | `DEV-game-finished-topic` | 게임 종료 이벤트 발행 |
| PROD | `PROD-payment-created-topic` | 결제 완료 이벤트 발행 |
| PROD | `PROD-game-finished-topic` | 게임 종료 이벤트 발행 |

### 사용 중인 SQS Queues

| 환경 | Queue 이름 | 구독 Topic | 용도 |
|------|-----------|-----------|------|
| DEV | `DEV-payment-created-queue` | DEV-payment-created-topic | 결제 완료 이벤트 처리 |
| DEV | `DEV-game-finished-queue` | DEV-game-finished-topic | 게임 종료 이벤트 처리 |
| PROD | `PROD-payment-created-queue` | PROD-payment-created-topic | 결제 완료 이벤트 처리 |
| PROD | `PROD-game-finished-queue` | PROD-game-finished-topic | 게임 종료 이벤트 처리 |

### DLQ (Dead Letter Queue) - 권장

| 환경 | DLQ 이름 | 연결된 Main Queue |
|------|---------|------------------|
| DEV | `DEV-payment-created-queue-dlq` | DEV-payment-created-queue |
| DEV | `DEV-game-finished-queue-dlq` | DEV-game-finished-queue |
| PROD | `PROD-payment-created-queue-dlq` | PROD-payment-created-queue |
| PROD | `PROD-game-finished-queue-dlq` | PROD-game-finished-queue |

---

## SNS/SQS 아키텍처

### 메시지 흐름

```
[이벤트 발생]
    ↓
[Spring @TransactionalEventListener]
    ↓
[OutboxEvent 저장 (DB)]
    ↓
[OutboxEventPublisher]
    ↓
[SNS Topic 발행]
    ↓
[SQS Queue 구독]
    ↓
[@SqsListener Consumer]
    ↓
[비즈니스 로직 처리]
    ↓
[OutboxEvent 완료 처리]
```

### Outbox Pattern 적용

- **트랜잭션 보장**: 비즈니스 로직과 이벤트 발행을 원자적으로 처리
- **멱등성**: OutboxEvent의 `published` 상태로 중복 처리 방지
- **재시도**: SQS의 Visibility Timeout을 활용한 자동 재시도

---

## 네이밍 규칙

### 1. 환경 Prefix

| 환경 | Prefix |
|------|--------|
| 개발 | `DEV-` |
| 운영 | `PROD-` |

### 2. SNS Topic 네이밍

**형식**: `{ENV}-{event-name}-topic`

**예시**:
- `DEV-payment-created-topic`
- `PROD-game-finished-topic`
- `DEV-user-registered-topic`

### 3. SQS Queue 네이밍

**형식**: `{ENV}-{event-name}-queue`

**예시**:
- `DEV-payment-created-queue`
- `PROD-game-finished-queue`
- `DEV-user-registered-queue`

### 4. DLQ 네이밍

**형식**: `{ENV}-{event-name}-queue-dlq`

**예시**:
- `DEV-payment-created-queue-dlq`
- `PROD-game-finished-queue-dlq`

### 5. Queue 타입

| 타입 | 사용 여부 | 이유 |
|------|----------|------|
| **Standard Queue** | ✅ 사용 | 순서 보장 불필요, 높은 처리량 필요 |
| FIFO Queue | ❌ 미사용 | 순서 보장 불필요, 비용 절감 |

---

## 새로운 SNS/SQS 추가 가이드

### Step 1: AWS 리소스 생성

#### 1-1. SNS Topic 생성

1. **AWS SNS 콘솔** 접속
2. **주제 생성** 클릭
3. 설정:
   - **유형**: 표준
   - **이름**: `{ENV}-{event-name}-topic` (예: `DEV-order-created-topic`)
   - **암호화**: 기본값 사용
4. **주제 생성** 완료
5. **ARN 복사** (나중에 환경 변수로 사용)

#### 1-2. SQS Queue 생성

1. **AWS SQS 콘솔** 접속
2. **대기열 생성** 클릭
3. 설정:
   - **유형**: 표준
   - **이름**: `{ENV}-{event-name}-queue` (예: `DEV-order-created-queue`)
   - **메시지 보존 기간**: 4일 (기본값)
   - **최대 메시지 크기**: 256 KB (기본값)
   - **기본 표시 제한 시간**: 30초
   - **메시지 수신 대기 시간**: 0초 (롱 폴링은 애플리케이션에서 설정)
4. **대기열 생성** 완료

#### 1-3. DLQ 생성 (권장)

1. **대기열 생성** 클릭
2. 설정:
   - **유형**: 표준
   - **이름**: `{ENV}-{event-name}-queue-dlq`
   - 나머지 기본값
3. **대기열 생성** 완료
4. **메인 큐(`{ENV}-{event-name}-queue`)** 선택 → **편집**
5. **배달 못한 편지 대기열** 섹션:
   - **활성화됨** 체크
   - **DLQ 선택**: 생성한 DLQ
   - **최대 수신 횟수**: 3 (3번 실패 시 DLQ 이동)
6. **저장**

#### 1-4. SNS → SQS 구독 설정

1. **생성한 SNS Topic** 선택
2. **구독 생성** 클릭
3. 설정:
   - **프로토콜**: Amazon SQS
   - **엔드포인트**: 생성한 SQS Queue ARN
   - **Raw message delivery**: ✅ **활성화** (중요!)
4. **구독 생성** 완료

**⚠️ 중요**: Raw message delivery를 활성화하지 않으면 메시지가 SNS Envelope로 감싸져서 역직렬화 실패합니다.

---

### Step 2: 코드 추가

#### 2-1. Enum 추가

**`JobName.java`** (또는 `OutboxEventType.java`)에 새 이벤트 타입 추가:

```java
public enum JobName {
    PAYMENT_CREATED("payment-created"),
    GAME_FINISHED("game-finished"),
    ORDER_CREATED("order-created");  // 👈 새로 추가

    private final String name;

    JobName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
```

#### 2-2. Message 클래스 생성

**`run_core/src/main/java/hyper/run/domain/outbox/message/OrderCreatedMessage.java`**:

```java
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedMessage {
    private String outboxEventId;
    private Long orderId;
    private Long userId;
    // 필요한 필드 추가

    public static OrderCreatedMessage from(String eventId, OrderData data) {
        return new OrderCreatedMessage(
            eventId,
            data.getOrderId(),
            data.getUserId()
        );
    }
}
```

#### 2-3. Processor 생성

**`run_core/src/main/java/hyper/run/domain/outbox/application/OrderCreatedProcessor.java`**:

```java
@Component
@RequiredArgsConstructor
public class OrderCreatedProcessor extends OutboxEventPublishProcessor {

    private final SnsPublisherService snsPublisherService;

    @Override
    protected void publish(String eventId, OutboxEventData data) {
        OrderData orderData = (OrderData) data;
        OrderCreatedMessage message = OrderCreatedMessage.from(eventId, orderData);
        snsPublisherService.publish(JobName.ORDER_CREATED, message, eventId);
    }

    @Override
    protected OutboxEventType getType() {
        return OutboxEventType.ORDER_CREATED;
    }
}
```

#### 2-4. Consumer 생성

**`run_core/src/main/java/hyper/run/domain/outbox/consumer/OrderCreatedConsumer.java`**:

```java
@Component
@ConditionalOnProperty(name = "cloud.aws.sqs.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedConsumer {

    private final OutboxEventRepository outboxEventRepository;
    // 필요한 Repository 추가

    @SqsListener(value = "${cloud.aws.sqs.queue.order-created}", messageVisibilitySeconds = "300")
    @Transactional
    public void consume(OrderCreatedMessage message) {
        log.info("Received message: {}", message);

        String outboxEventId = message.getOutboxEventId();

        try {
            // STEP 1: OutboxEvent 조회 및 멱등성 체크
            OutboxEvent outboxEvent = outboxEventRepository.findById(outboxEventId)
                    .orElseThrow(() -> new IllegalStateException("OutboxEvent not found: " + outboxEventId));

            if (outboxEvent.isPublished()) {
                return;
            }

            // STEP 2: 비즈니스 로직 처리
            // ...

            // STEP 3: OutboxEvent 완료 처리
            outboxEvent.publish();

            log.info("Order processed successfully. outboxEventId: {}, orderId: {}",
                    outboxEventId, message.getOrderId());

        } catch (Exception e) {
            log.error("Failed to process order created message. outboxEventId: {}, orderId: {}",
                    outboxEventId, message.getOrderId(), e);
            throw e;
        }
    }
}
```

---

### Step 3: 환경 변수 설정

#### 3-1. `.env` 파일에 추가

```bash
# 새로운 SNS Topic ARN
AWS_SNS_ORDER_CREATED_TOPIC_ARN=arn:aws:sns:ap-northeast-2:434674112747:DEV-order-created-topic

# 새로운 SQS Queue 이름
AWS_SQS_ORDER_CREATED_QUEUE_NAME=DEV-order-created-queue
```

#### 3-2. `aws-sns-sqs-config-dev.yml`에 추가

```yaml
cloud:
  aws:
    sqs:
      queue:
        payment-created: ${AWS_SQS_PAYMENT_CREATED_QUEUE_NAME:DEV-payment-created-queue}
        game-finished: ${AWS_SQS_GAME_FINISHED_QUEUE_NAME:DEV-game-finished-queue}
        order-created: ${AWS_SQS_ORDER_CREATED_QUEUE_NAME:DEV-order-created-queue}  # 👈 추가
    sns:
      topic:
        payment-created: ${AWS_SNS_PAYMENT_CREATED_TOPIC_ARN:}
        game-finished: ${AWS_SNS_GAME_FINISHED_TOPIC_ARN:}
        order-created: ${AWS_SNS_ORDER_CREATED_TOPIC_ARN:}  # 👈 추가
```

#### 3-3. `aws-sns-sqs-config-prod.yml`에도 동일하게 추가

```yaml
cloud:
  aws:
    sqs:
      queue:
        # ...
        order-created: ${AWS_SQS_ORDER_CREATED_QUEUE_NAME:PROD-order-created-queue}
    sns:
      topic:
        # ...
        order-created: ${AWS_SNS_ORDER_CREATED_TOPIC_ARN:}
```

---

## 환경 변수 설정

### 현재 설정된 환경 변수

#### DEV 환경

```bash
# AWS 인증
AWS_SQS_ACCESS_KEY=<your-access-key>
AWS_SQS_SECRET_KEY=<your-secret-key>

# SNS Topic ARN
AWS_SNS_PAYMENT_CREATED_TOPIC_ARN=arn:aws:sns:ap-northeast-2:434674112747:DEV-payment-created-topic
AWS_SNS_GAME_FINISHED_TOPIC_ARN=arn:aws:sns:ap-northeast-2:434674112747:DEV-game-finished-topic

# SQS Queue 이름
AWS_SQS_PAYMENT_CREATED_QUEUE_NAME=DEV-payment-created-queue
AWS_SQS_GAME_FINISHED_QUEUE_NAME=DEV-game-finished-queue
```

#### PROD 환경

```bash
# SNS Topic ARN
AWS_SNS_PAYMENT_CREATED_TOPIC_ARN=arn:aws:sns:ap-northeast-2:434674112747:PROD-payment-created-topic
AWS_SNS_GAME_FINISHED_TOPIC_ARN=arn:aws:sns:ap-northeast-2:434674112747:PROD-game-finished-topic

# SQS Queue 이름
AWS_SQS_PAYMENT_CREATED_QUEUE_NAME=PROD-payment-created-queue
AWS_SQS_GAME_FINISHED_QUEUE_NAME=PROD-game-finished-queue
```

---

## SQS Listener 설정

현재 프로젝트는 **Spring Boot Auto Configuration**을 사용합니다.

### 설정 값 (`application.yml`)

```yaml
cloud:
  aws:
    sqs:
      listener:
        max-concurrent-messages: 10      # 동시 처리 메시지 수
        max-messages-per-poll: 10        # 한 번에 가져올 메시지 수 (최대 10)
        poll-timeout: 20                 # 롱 폴링 대기 시간 (초)
        visibility-timeout: 300          # 메시지 처리 제한 시간 (초, 5분)
```

### 설정 값 설명

| 설정 | 기본값 | 권장값 | 설명 |
|------|--------|--------|------|
| `max-concurrent-messages` | 10 | 10 | 동시에 처리할 수 있는 최대 메시지 수. 너무 크면 부하 증가 |
| `max-messages-per-poll` | 10 | 10 | 한 번의 폴링에서 가져올 메시지 수 (SQS 최대값) |
| `poll-timeout` | 10 | 20 | 롱 폴링 대기 시간. 길수록 API 호출 비용 절감 |
| `visibility-timeout` | 30 | 300 | 메시지 처리 제한 시간. 처리 시간보다 길게 설정 |

---

## 트러블슈팅

### 1. "Topic not configured" 에러

**원인**: 환경 변수가 설정되지 않음

**해결**:
1. `.env` 파일에 `AWS_SNS_{EVENT}_TOPIC_ARN` 추가
2. Docker 재시작: `docker-compose down && docker-compose up -d`

### 2. "JsonParseException: Unrecognized token 'hyper'" 에러

**원인**: SNS → SQS 구독에서 "Raw message delivery"가 비활성화됨

**해결**:
1. AWS SNS 콘솔 → 해당 Topic → 구독 선택
2. "Raw message delivery" 활성화
3. SQS 큐 Purge (기존 메시지 삭제)

### 3. 메시지가 계속 재시도됨

**원인**:
- Consumer에서 예외 발생
- DLQ 미설정

**해결**:
1. 로그에서 에러 원인 확인
2. DLQ 설정 (위 가이드 참고)
3. maxReceiveCount 설정 (권장: 3)

### 4. 이전 실패 메시지 정리

**AWS 콘솔에서**:
1. SQS 콘솔 → 해당 큐 선택
2. "제거" 버튼 클릭 (Purge)
3. 확인

**또는 Docker 중지 후 정리**:
```bash
docker-compose down
# AWS 콘솔에서 큐 Purge
# 30초 대기
docker-compose up -d
```

---

## 참고 자료

- [Spring Cloud AWS 공식 문서](https://docs.awspring.io/spring-cloud-aws/docs/current/reference/html/index.html)
- [AWS SQS 개발자 가이드](https://docs.aws.amazon.com/sqs/)
- [AWS SNS 개발자 가이드](https://docs.aws.amazon.com/sns/)
