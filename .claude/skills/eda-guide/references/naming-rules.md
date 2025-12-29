# 네이밍 규칙 상세

## 이벤트 네이밍

| 대상 | 패턴 | 사용처 | 예시 |
|------|------|--------|------|
| Spring Event | `xxxEvent` | ApplicationEventPublisher로 발행 | `GameApplyEvent`, `PaymentCreatedEvent` |
| SNS 메시지 | `xxxMessage` | SNS Topic으로 발행 | `PaymentCreatedMessage`, `GameFinishedMessage` |
| Outbox 페이로드 | `xxxPayload` | OutboxEvent에 저장 | `PaymentJobPayload`, `GameJobPayload` |

### 예시: Payment 도메인

```
payment/event/
├── PaymentCreatedEvent.java    (Spring Event)
├── PaymentCreatedMessage.java  (SNS 메시지)
└── PaymentJobPayload.java      (Outbox 페이로드)
```

## 리스너 네이밍

| 대상 | 패턴 | 어노테이션 | 예시 |
|------|------|-----------|------|
| Spring Event 리스너 | `xxxListener` | @EventListener, @TransactionalEventListener | `PaymentListener`, `GameApplyListener` |
| SQS Consumer | `xxxConsumer` | @SqsListener | `PaymentCreatedConsumer`, `GameFinishedConsumer` |

### 차이점

**xxxListener**:
- Spring 내부 이벤트 처리
- 동일 애플리케이션 내
- OutboxEvent 저장 담당

**xxxConsumer**:
- SQS 메시지 소비
- 외부 메시지 처리
- 실제 비즈니스 로직 실행

## Processor 네이밍

| 대상 | 패턴 | 역할 | 예시 |
|------|------|------|------|
| SNS Publisher | `xxxProcessor` | OutboxEvent를 SNS로 발행 | `PaymentCreatedProcessor`, `GameFinishedProcessor` |
| Job Processor | `xxxJobProcessor` | SQS 메시지 처리 (비즈니스 로직) | `PaymentCreatedJobProcessor`, `GameFinishedJobProcessor` |

### 차이점

**xxxProcessor**:
- OutboxEventPublishProcessor 상속
- SNS 발행 전용
- 단순 메시지 변환 및 발행

**xxxJobProcessor**:
- JobProcessor 인터페이스 구현
- 비즈니스 로직 실행
- 멱등성 체크 포함

## JobType Enum

### 정의
SNS/SQS에 발행할 메시지 타입을 정의하는 Enum

### 규칙
- 새로운 Message 추가 시 반드시 JobType도 추가
- 대문자 스네이크 케이스 사용
- 도메인_액션 형식

### 예시

```java
public enum JobType {
    PAYMENT_CREATED,      // 결제 생성
    PAYMENT_REFUNDED,     // 결제 환불
    GAME_FINISHED,        // 경기 종료
    PRIZE_DISTRIBUTED    // 상금 지급
}
```

## 전체 네이밍 예시

### Payment 도메인 (SNS/SQS 사용)

```
domain/payment/
├── event/
│   ├── PaymentCreatedEvent.java      (Spring Event)
│   ├── PaymentJobPayload.java        (Outbox 저장)
│   └── PaymentCreatedMessage.java    (SNS 발행)
├── listener/
│   └── PaymentListener.java          (Spring 리스너)
├── producer/
│   └── PaymentCreatedProcessor.java  (SNS Publisher)
├── consumer/
│   └── PaymentCreatedConsumer.java   (SQS Consumer)
└── application/
    └── PaymentCreatedJobProcessor.java (비즈니스 로직)
```

### Game 도메인 (Spring Event만 사용)

```
domain/game/
├── event/
│   ├── GameApplyEvent.java
│   └── GameCancelEvent.java
└── listener/
    ├── GameApplyListener.java
    └── GameCancelListener.java
```
