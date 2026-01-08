# Outbox 패턴 상세

## 목적

금전 거래의 **데이터 유실 방지** 및 **최종 일관성 보장**

## 해결하는 문제

### 기존 문제 (Outbox 없이)

```
비즈니스 데이터 저장 → SNS 발행
                ↓
          SNS 발행 실패!
          → 데이터는 있는데 이벤트 유실
```

### Outbox 패턴 적용

```
비즈니스 데이터 + OutboxEvent 저장 (같은 트랜잭션)
                ↓
         트랜잭션 커밋 성공
                ↓
          SNS 발행 (별도)
          → 실패해도 OutboxEvent가 DB에 있어서 재시도 가능
```

## 동작 흐름

### 1단계: 트랜잭션 내 저장
```
@Transactional
public void createPayment(...) {
    // 1. Payment 저장
    Payment payment = paymentRepository.save(payment);

    // 2. @PostPersist → PaymentCreatedEvent 발행
    // 3. PaymentListener → OutboxEvent 저장
    //
    // ✅ Payment와 OutboxEvent 모두 같은 트랜잭션
    // → 둘 다 성공 or 둘 다 실패
}
```

### 2단계: 트랜잭션 외부 SNS 발행
```
@TransactionalEventListener(AFTER_COMMIT)
public void publish(OutboxCommittedEvent event) {
    // 트랜잭션 커밋 후 실행
    // SNS 발행
    // publishedToQueue = true 표시
}
```

### 3단계: SQS Consumer 처리
```
@SqsListener("payment-queue")
public void consume(PaymentCreatedMessage message) {
    // 1. OutboxEvent 조회
    // 2. isPublished 체크 (멱등성)
    // 3. 비즈니스 로직 실행
    // 4. isPublished = true
}
```

## 주요 컴포넌트

### OutboxEvent

**역할**: 이벤트 메타데이터 저장

**주요 필드**:
- `id`: UUID (이벤트 고유 ID)
- `type`: JobType (이벤트 타입)
- `isPublished`: 비즈니스 처리 완료 여부 (멱등성)
- `publishedToQueue`: SNS 발행 완료 여부
- `publishedToQueueAt`: SNS 발행 시각
- `retryCount`: 재시도 횟수
- `data`: JobEventPayload (이벤트 데이터 JSON)

### OutboxEventPublisher

**역할**: 트랜잭션 커밋 후 SNS 발행 조율

**동작**:
1. `OutboxCommittedEvent` 수신
2. JobType에 맞는 Processor 찾기
3. Processor.publish() 호출
4. 성공 시 `markPublishedToQueue()` 호출

### OutboxEventPublishProcessor

**역할**: 각 이벤트 타입별 SNS 발행 로직

**구현 예시**:
```java
@Component
public class PaymentCreatedProcessor extends OutboxEventPublishProcessor {

    @Override
    protected void publish(String eventId, JobEventPayload data) {
        PaymentJobPayload payload = (PaymentJobPayload) data;
        PaymentCreatedMessage message = PaymentCreatedMessage.from(eventId, payload);
        snsPublisherService.publish(JobType.PAYMENT_CREATED, message, eventId);
    }

    @Override
    protected JobType getType() {
        return JobType.PAYMENT_CREATED;
    }
}
```

## 멱등성 보장

### isPublished 플래그

```java
@Transactional
public void process(PaymentCreatedMessage message) {
    OutboxEvent outboxEvent = repository.findById(message.getOutboxEventId());

    // ✅ 멱등성 체크
    if (outboxEvent.isPublished()) {
        log.info("Already processed");
        return; // 중복 처리 방지
    }

    // 비즈니스 로직 실행
    user.increaseCoupon();
    payment.updateState(COMPLETED);

    // 완료 표시
    outboxEvent.publish();
}
```

## 장애 시나리오

### SNS 발행 실패
- OutboxEvent는 DB에 저장됨
- `publishedToQueue = false`
- 재시도 메커니즘으로 복구

### Consumer 처리 중 실패
- SQS 메시지 삭제 안됨
- Visibility Timeout 후 재수신
- 멱등성 체크로 중복 방지

### 애플리케이션 재시작
- DB에 OutboxEvent 보존
- 재시작 후 배치로 재발행

## 왜 금전 거래에만 사용?

**Outbox 패턴은 복잡도가 높습니다:**
- 테이블 추가 (OutboxEvent)
- 7개 클래스 구현 필요
- 배치 재시도 메커니즘 필요

**따라서:**
- 금전 거래 (결제, 상금, 환불): 필수
- 일반 이벤트 (경기 신청, 알림): Spring Event로 충분
