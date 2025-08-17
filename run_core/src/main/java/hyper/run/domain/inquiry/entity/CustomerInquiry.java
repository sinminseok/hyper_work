package hyper.run.domain.inquiry.entity;

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
public class CustomerInquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_inquiry_id", updatable = false)
    private Long id;

    @Column(name = "payment_id",nullable = true)
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", updatable = false, nullable = false)
    private InquiryType type; // 문의 유형

    @Column(name = "refund_price", nullable = true)
    private Integer refundPrice; // 환불 가격 (문의 유형이 환불일 경우)

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_type", nullable = true)
    private RefundType refundType; // 환불 이유 (문의 유형이 환불일 경우)

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private InquiryState state; // 문의 상태

    @Column(name = "title",nullable = false)
    private String title; // 문의명

    @Column(name = "message", updatable = false, nullable = false)
    private String message; // 문의내용

    @Setter
    @Column(name = "answer",nullable = true)
    private String answer; // 답변 내용

    @Column(name = "inquired_at",nullable = false)
    private LocalDate inquiredAt; // 문의일
}
