package hyper.run.domain.payment.entity;

public enum PaymentState {
    PAYMENT_COMPLETED,   // 결제 완료
    REFUND_REQUESTED,    // 환불 요청 중
    REFUND_REJECTED,     // 환불 거절
    REFUND_COMPLETED;    // 환불 완료
}
