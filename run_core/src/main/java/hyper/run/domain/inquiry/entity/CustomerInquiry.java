package hyper.run.domain.inquiry.entity;

import hyper.run.domain.common.BaseTimeEntity;
import hyper.run.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Table(name = "customer_inquiry")
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerInquiry extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_inquiry_id", updatable = false)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "payment_id", nullable = true)
    private Long paymentId; // 문의한 결제 id

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

    @Column(name = "account_number", nullable = true)
    private String accountNumber; // 환불 받을 계좌 번호

    @Column(name = "bank_name", nullable = true)
    private String bankName; // 환불 받을 은행 이름

    @Column(name = "message", updatable = false, nullable = false)
    private String message;

    @Column(name = "title",nullable = false)
    private String title; // 문의명

    @Column(name = "answer",nullable = true)
    private String answer; // 답변 내용

}
