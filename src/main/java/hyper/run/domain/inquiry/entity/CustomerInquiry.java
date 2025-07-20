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
    private String email;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", updatable = false, nullable = false)
    private InquiryType type;

    @Column(name = "refund_price", nullable = true)
    private Integer refundPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_type", nullable = true)
    private RefundType refundType;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private InquiryState state;

    @Column(name = "message", updatable = false, nullable = false)
    private String message;
}
