package hyper.run.domain.payment.entity;

import hyper.run.domain.payment.dto.response.AppleTransactionResponse;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = "transactionId")
})
@Getter
@NoArgsConstructor
public class AppleTransaction {

    @Id
    @GeneratedValue
    private Long id;

    private String transactionId;
    private String productId;
    private Instant purchasedAt;

    public static AppleTransaction of(AppleTransactionResponse r) {
        AppleTransaction t = new AppleTransaction();
        t.transactionId = r.getTransactionId();
        t.productId = r.getProductId();
        t.purchasedAt = r.getPurchaseDate();
        return t;
    }
}