package hyper.run.domain.exchange_transaction.entity;


import hyper.run.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeTransaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exchange_transaction_id", updatable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "exchange_status", nullable = false)
    private ExchangeStatus exchangeStatus;


}
