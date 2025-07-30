package hyper.run.domain.inquiry.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "customer_inquiry")
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerInquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_inquiry_id", updatable = false)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email; // 문의한 사용자 email

    @Column(name = "user_id", nullable = false)
    private Long userId; // 문의한 사용자 id (pk)

    @Enumerated(EnumType.STRING)
    @Column(name = "type", updatable = false, nullable = false)
    private InquiryType type; // 문의 유형

    @Column(name = "refund_price", nullable = true)
    private Integer refundPrice; // 환불 가격 (문의 유형이 환불일 경우)

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_type", nullable = true)
    private RefundType refundType; // 환불 이유 (문의 유형이 환불일 경우)

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private InquiryState state; // 문의 상태

    @Column(name = "message", updatable = false, nullable = false)
    private String message;
}
