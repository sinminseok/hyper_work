# 새 이벤트 추가 가이드

## 1. Spring Event만 사용 (일반 이벤트)

### 적용 대상
- 경기 신청, 취소
- 알림 발송
- 통계 업데이트
- 부가 작업이 적은 경우

### 구현 단계

#### Step 1: Event 클래스 생성
**위치**: `domain/xxx/event/XxxEvent.java`

```java
public class GameApplyEvent {
    private final Long userId;
    private final Long gameId;
    private final Integer averageBpm;
    private final Integer targetCadence;

    public static GameApplyEvent from(Long userId, Long gameId, ...) {
        return new GameApplyEvent(userId, gameId, ...);
    }
}
```

#### Step 2: 엔티티에서 이벤트 발행
**방법 A**: `registerEvent()` 사용 (비즈니스 메서드)

```java
@Entity
public class Game extends BaseTimeEntity<Game> {

    public void applyGame(Long userId, Integer averageBpm, Integer targetCadence) {
        increaseParticipatedCount();
        registerEvent(GameApplyEvent.from(userId, this.getId(), averageBpm, targetCadence));
    }
}
```

**방법 B**: `@PostPersist` 사용 (생명주기)

```java
@Entity
public class Game {

    @PostPersist
    private void publishGameCreatedEvent() {
        registerEvent(GameCreatedEvent.from(this));
    }
}
```

#### Step 3: Listener 클래스 생성
**위치**: `domain/xxx/listener/XxxListener.java`

```java
@Component
@Slf4j
public class GameApplyListener {

    private final GameHistoryRepository gameHistoryRepository;
    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createGameHistory(GameApplyEvent event) {
        GameHistory history = GameHistory.create(
            event.getUserId(),
            event.getGameId(),
            event.getAverageBpm(),
            event.getTargetCadence()
        );
        gameHistoryRepository.save(history);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void sendNotification(GameApplyEvent event) {
        notificationService.sendGameApplyNotification(event.getUserId());
    }
}
```

### 완성된 구조

```
domain/game/
├── event/
│   ├── GameApplyEvent.java
│   └── GameCancelEvent.java
└── listener/
    ├── GameApplyListener.java
    └── GameCancelListener.java
```

---

## 2. SNS/SQS 사용 (금전 거래)

### 적용 대상
- 결제 처리
- 상금 지급
- 환불 처리
- 쿠폰 발급

### 구현 단계

#### Step 1: JobType enum 추가
**위치**: `common/enums/JobType.java`

```java
public enum JobType {
    PAYMENT_CREATED,
    PRIZE_DISTRIBUTED,
    REFUND_PROCESSED  // 추가
}
```

#### Step 2: Payload 클래스 생성
**위치**: `domain/xxx/event/XxxPayload.java`

```java
public class RefundJobPayload implements JobEventPayload {
    private Long refundId;
    private Long userId;
    private Integer amount;
    private String reason;

    // constructor, getters
}
```

#### Step 3: Message 클래스 생성
**위치**: `domain/xxx/event/XxxMessage.java`

```java
public class RefundProcessedMessage {
    private String outboxEventId;  // 추적용
    private Long refundId;
    private Long userId;
    private Integer amount;
    private String reason;

    public static RefundProcessedMessage from(String outboxEventId, RefundJobPayload payload) {
        return new RefundProcessedMessage(
            outboxEventId,
            payload.getRefundId(),
            payload.getUserId(),
            payload.getAmount(),
            payload.getReason()
        );
    }
}
```

#### Step 4: Event 클래스 생성 (Spring Event)
**위치**: `domain/xxx/event/XxxEvent.java`

```java
public class RefundCreatedEvent {
    private final Long refundId;
    private final Long userId;
    private final Integer amount;

    public static RefundCreatedEvent from(Refund refund) {
        return new RefundCreatedEvent(
            refund.getId(),
            refund.getUserId(),
            refund.getAmount()
        );
    }
}
```

#### Step 5: Listener 생성 (OutboxEvent 저장)
**위치**: `domain/xxx/listener/XxxListener.java`

```java
@Component
@RequiredArgsConstructor
public class RefundListener {

    private final OutboxEventRepository outboxEventRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleRefundCreated(RefundCreatedEvent event) {
        RefundJobPayload payload = new RefundJobPayload(
            event.getRefundId(),
            event.getUserId(),
            event.getAmount(),
            event.getReason()
        );

        OutboxEvent outboxEvent = new OutboxEvent(JobType.REFUND_PROCESSED, payload);
        outboxEventRepository.save(outboxEvent);
    }
}
```

#### Step 6: Processor 생성 (SNS 발행)
**위치**: `domain/xxx/producer/XxxProcessor.java`

```java
@Component
@RequiredArgsConstructor
public class RefundProcessedProcessor extends OutboxEventPublishProcessor {

    private final SnsPublisherService snsPublisherService;

    @Override
    protected void publish(String eventId, JobEventPayload data) {
        RefundJobPayload payload = (RefundJobPayload) data;
        RefundProcessedMessage message = RefundProcessedMessage.from(eventId, payload);
        snsPublisherService.publish(JobType.REFUND_PROCESSED, message, eventId);
    }

    @Override
    protected JobType getType() {
        return JobType.REFUND_PROCESSED;
    }
}
```

#### Step 7: Consumer 생성 (SQS 수신)
**위치**: `domain/xxx/consumer/XxxConsumer.java`

```java
@Component
@ConditionalOnProperty(name = "cloud.aws.sqs.enabled", havingValue = "true")
@RequiredArgsConstructor
public class RefundProcessedConsumer {

    private final JobProcessorFactory jobProcessorFactory;

    @SqsListener(value = "${cloud.aws.sqs.queue.refund-processed}", messageVisibilitySeconds = "300")
    public void consume(RefundProcessedMessage message) {
        JobProcessor<RefundProcessedMessage> processor =
            jobProcessorFactory.getProcessor(JobType.REFUND_PROCESSED);
        processor.process(message, null);
    }
}
```

#### Step 8: JobProcessor 생성 (비즈니스 로직)
**위치**: `domain/xxx/application/XxxJobProcessor.java`

```java
@Component
@RequiredArgsConstructor
public class RefundProcessedJobProcessor implements JobProcessor<RefundProcessedMessage> {

    private final OutboxEventRepository outboxEventRepository;
    private final RefundRepository refundRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void process(RefundProcessedMessage message, Visibility visibility) {
        String outboxEventId = message.getOutboxEventId();

        // STEP 1: 멱등성 체크
        OutboxEvent outboxEvent = outboxEventRepository.findById(outboxEventId)
            .orElseThrow(() -> new IllegalStateException("OutboxEvent not found"));

        if (outboxEvent.isPublished()) {
            log.info("Already processed: {}", outboxEventId);
            return;
        }

        // STEP 2: Refund 조회
        Refund refund = refundRepository.findById(message.getRefundId())
            .orElseThrow(() -> new IllegalStateException("Refund not found"));

        // STEP 3: User 포인트 환불
        User user = userRepository.findById(message.getUserId())
            .orElseThrow(() -> new IllegalStateException("User not found"));

        user.increasePoints(message.getAmount());

        // STEP 4: Refund 상태 변경
        refund.updateState(RefundState.COMPLETED);

        // STEP 5: OutboxEvent 완료 처리
        outboxEvent.publish();

        log.info("Refund processed: {}", outboxEventId);
    }

    @Override
    public JobType getType() {
        return JobType.REFUND_PROCESSED;
    }
}
```

### 완성된 구조

```
domain/refund/
├── event/
│   ├── RefundCreatedEvent.java       (Spring Event)
│   ├── RefundJobPayload.java         (Outbox 데이터)
│   └── RefundProcessedMessage.java   (SNS 메시지)
├── listener/
│   └── RefundListener.java           (OutboxEvent 저장)
├── producer/
│   └── RefundProcessedProcessor.java (SNS 발행)
├── consumer/
│   └── RefundProcessedConsumer.java  (SQS 수신)
└── application/
    └── RefundProcessedJobProcessor.java (비즈니스 로직)
```

---

## 체크리스트

### Spring Event

- [ ] Event 클래스 생성 (`xxxEvent`)
- [ ] Listener 클래스 생성 (`xxxListener`)
- [ ] 엔티티에서 `registerEvent()` 호출
- [ ] @TransactionalEventListener 사용
- [ ] 필요 시 @Async 추가

### SNS/SQS (Outbox 패턴)

- [ ] JobType enum 추가
- [ ] Payload 클래스 생성 (`xxxPayload`, JobEventPayload 구현)
- [ ] Message 클래스 생성 (`xxxMessage`)
- [ ] Event 클래스 생성 (`xxxEvent`)
- [ ] Listener에서 OutboxEvent 저장 (@TransactionalEventListener BEFORE_COMMIT)
- [ ] Processor에서 SNS 발행 구현 (OutboxEventPublishProcessor 상속)
- [ ] Consumer에서 SQS 수신 구현 (@SqsListener)
- [ ] JobProcessor에서 비즈니스 로직 구현 (JobProcessor 인터페이스)
- [ ] 멱등성 체크 로직 추가 (OutboxEvent.isPublished)
- [ ] AWS 설정: application.yml에 Topic/Queue 추가

## 중요 원칙

1. **금전 거래가 아니면 Spring Event 사용** → 단순하고 빠름
2. **금전 거래면 반드시 Outbox + SNS/SQS 사용** → 안전하고 추적 가능
3. **멱등성 보장 필수** → isPublished 체크
4. **트랜잭션 경계 명확히** → BEFORE_COMMIT vs AFTER_COMMIT
5. **조회는 이벤트 사용 안 함** → 상태 변경 없음
