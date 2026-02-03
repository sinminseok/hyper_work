package hyper.run.domain.exchange_transaction.dto.request;

import hyper.run.domain.exchange_transaction.entity.ExchangeStatus;
import hyper.run.domain.exchange_transaction.entity.ExchangeTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeTransactionRequest {

    private Long userId;

    private double amount;

    private String accountNumber;

    private String bankName;

    public ExchangeTransaction toEntity(){
        return ExchangeTransaction
                .builder()
                .userId(this.userId)
                .amount(this.amount)
                .exchangeStatus(ExchangeStatus.REQUESTED)
                .accountNumber(this.accountNumber)
                .bankName(this.bankName)
                .build();
    }
}
