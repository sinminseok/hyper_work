package hyper.run.domain.payment.entity;

/**
 * 결제 상태 Enum
 *
 * 상태 전이 흐름:
 * 1. 정상 플로우: PENDING -> PAYMENT_COMPLETED
 * 2. 환불 플로우: PAYMENT_COMPLETED -> REFUND_REQUESTED -> (REFUND_REJECTED | REFUND_COMPLETED)
 *
 * 주의사항:
 * - FAILED 상태는 현재 미사용 (영수증 검증 실패 시 Payment 자체가 생성되지 않음)
 * - PENDING 상태는 영수증 검증 완료 + 쿠폰 지급 대기 중을 의미
 */
public enum PaymentState {
    /**
     * 영수증 검증 완료, 쿠폰 지급 대기 중
     * PaymentService.pay()에서 Payment 생성 시 설정됨
     */
    PENDING,

    /**
     * 쿠폰 지급 완료 (정상 완료 상태)
     * PaymentCreatedProcessor에서 쿠폰 지급 후 설정됨
     */
    PAYMENT_COMPLETED,

    /**
     * 결제 실패 (현재 미사용)
     * 영수증 검증 실패 시 Payment가 생성되지 않으므로 이 상태는 사용되지 않음
     */
    FAILED,

    /**
     * 환불 요청됨
     * 사용자가 환불 요청 시 설정됨
     */
    REFUND_REQUESTED,

    /**
     * 환불 거절됨
     * 관리자가 환불 요청을 거절 시 설정됨
     */
    REFUND_REJECTED,

    /**
     * 환불 완료
     * 환불 처리가 완료되면 설정됨
     */
    REFUND_COMPLETED;
}
