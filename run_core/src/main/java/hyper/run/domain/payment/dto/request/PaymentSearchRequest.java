package hyper.run.domain.payment.dto.request;

import hyper.run.domain.payment.entity.PaymentState;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PaymentSearchRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer minAmount;
    private Integer maxAmount;
    private PaymentState state;
    private String keyword;
}
