package hyper.run.domain.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppleTransactionResponse {

    private String transactionId;
    private String productId;
    private Instant purchaseDate;
    private Instant revocationDate;
    private String environment;
    private String status;
}