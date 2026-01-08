# run_core 이벤트 관련 클래스 역할

## 패키지 구조

```
run_core/src/main/java/hyper/run/domain/
├── outbox/                    (공통)
│   ├── entity/
│   │   ├── OutboxEvent
│   │   └── OutboxCommittedEvent
│   ├── application/
│   │   ├── OutboxEventPublisher
│   │   └── OutboxEventPublishProcessor
│   └── repository/
│       └── OutboxEventRepository
│
├── payment/                   (도메인 예시)
│   ├── event/
│   │   ├── PaymentCreatedEvent
│   │   ├── PaymentCreatedMessage
│   │   └── PaymentJobPayload
│   ├── listener/
│   │   └── PaymentListener
│   ├── producer/
│   │   └── PaymentCreatedProcessor
│   ├── consumer/
│   │   └── PaymentCreatedConsumer
│   └── application/
│       └── PaymentCreatedJobProcessor
│
└── game/                      (도메인 예시)
    ├── event/
    │   ├── GameApplyEvent
    │   └── GameCancelEvent
    └── listener/
        ├── GameApplyListener
        └── GameCancelListener
```

## 공통 클래스 (domain/outbox/)

### OutboxEvent (Entity)

**역할**: 모든 이벤트의 메타데이터 저장

**책임**:
- 이벤트 데이터 영구 보관
- 발행 상태 관리 (isPublished, publishedToQueue)
- 재시도 정보 추적 (retryCount, lastRetryAt)

**사용 시점**: Payment, Prize 등 금전 거래 이벤트 저장

### OutboxEventPublisher (Application Service)

**역할**: 트랜잭션 커밋 후 이벤트 발행 조율

**책임**:
- `OutboxCommittedEvent` 수신
- JobType에 맞는 Processor 찾기
- SNS 발행 성공 시 `markPublishedToQueue()` 호출

**트리거**: @TransactionalEventListener(AFTER_COMMIT)

### OutboxEventPublishProcessor (Abstract Class)

**역할**: 각 이벤트 타입별 SNS 발행 로직 정의

**책임**:
- `publish(eventId, data)` 구현
- `getType()` 구현 (JobType 반환)

**구현체**: PaymentCreatedProcessor, GameFinishedProcessor 등

### OutboxEventRepository

**역할**: OutboxEvent CRUD

**주요 메서드**:
- `findById(String id)`
- `save(OutboxEvent)`

## 도메인 이벤트 (domain/xxx/event/)

### xxxEvent (Spring Event)

**역할**: Spring 내부 이벤트

**책임**:
- 도메인 이벤트 데이터 전달
- @EventListener 트리거

**특징**:
- Spring ApplicationEventPublisher로 발행
- 동기 처리
- 트랜잭션 내부

**예시**: `PaymentCreatedEvent`, `GameApplyEvent`

### xxxMessage (SNS 메시지)

**역할**: SNS로 발행할 메시지 DTO

**책임**:
- 외부 시스템에 전달할 데이터 정의
- JSON 직렬화 가능

**특징**:
- Outbox패턴의 최종 발행 메시지
- outboxEventId 포함 (추적용)

**예시**: `PaymentCreatedMessage`, `GameFinishedMessage`

### xxxPayload (Outbox 데이터)

**역할**: OutboxEvent에 저장될 이벤트 데이터

**책임**:
- JobEventPayload 인터페이스 구현
- 비즈니스 데이터 보관

**특징**:
- OutboxEvent.data 필드에 JSON 저장
- 나중에 Message로 변환

**예시**: `PaymentJobPayload`, `GameJobPayload`

## 리스너 (domain/xxx/listener/ 또는 consumer/)

### xxxListener (Spring Event 리스너)

**역할**: Spring Event를 받아 OutboxEvent 저장

**책임**:
- PaymentCreatedEvent 수신
- Payload 생성
- OutboxEvent 저장 (같은 트랜잭션)

**어노테이션**: `@TransactionalEventListener(BEFORE_COMMIT)`

**특징**:
- 트랜잭션 커밋 전 실행
- Payment와 OutboxEvent 원자적 저장

**예시**: `PaymentListener`, `GameApplyListener`

### xxxConsumer (SQS Consumer)

**역할**: SQS 메시지를 받아 처리

**책임**:
- SQS 메시지 수신
- JobProcessor 호출

**어노테이션**: `@SqsListener("queue-name")`

**특징**:
- 비동기 처리
- 메시지 재시도 자동 지원

**예시**: `PaymentCreatedConsumer`, `GameFinishedConsumer`

## 프로세서 (domain/xxx/producer/ 또는 application/)

### xxxProcessor (SNS Publisher)

**역할**: OutboxEvent를 SNS로 발행

**책임**:
- OutboxEventPublishProcessor 상속
- Payload → Message 변환
- SNS 발행

**특징**:
- 트랜잭션 외부에서 실행
- 발행 실패 시 재시도 가능

**예시**: `PaymentCreatedProcessor`, `GameFinishedProcessor`

### xxxJobProcessor (비즈니스 로직)

**역할**: SQS 메시지를 받아 실제 비즈니스 로직 실행

**책임**:
- JobProcessor<Message> 구현
- 멱등성 체크
- 비즈니스 로직 실행 (쿠폰 지급, 상태 변경 등)

**특징**:
- 새 트랜잭션에서 실행
- OutboxEvent.isPublished로 중복 방지

**예시**: `PaymentCreatedJobProcessor`, `GameFinishedJobProcessor`

## 실행 흐름 요약

### Spring Event 방식 (게임)

```
Game.applyGame()
  ↓ registerEvent()
GameApplyEvent 발행
  ↓ @EventListener
GameApplyListener 실행
  ↓
GameHistory 생성, 알림 발송
```

### Outbox + SNS/SQS 방식 (결제)

```
Payment 저장
  ↓ @PostPersist
PaymentCreatedEvent 발행
  ↓ @TransactionalEventListener(BEFORE_COMMIT)
PaymentListener → OutboxEvent 저장
  ↓ 트랜잭션 커밋
  ↓ @TransactionalEventListener(AFTER_COMMIT)
OutboxEventPublisher → PaymentCreatedProcessor
  ↓ SNS 발행
PaymentCreatedMessage → SQS
  ↓ @SqsListener
PaymentCreatedConsumer → PaymentCreatedJobProcessor
  ↓
쿠폰 지급, 상태 변경
```
