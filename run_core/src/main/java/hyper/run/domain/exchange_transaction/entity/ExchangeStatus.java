package hyper.run.domain.exchange_transaction.entity;

public enum ExchangeStatus {
    REQUESTED,   // 환전 신청됨
    CANCELLED,   // 환전 취소됨
    COMPLETED    // 환전 완료됨
}