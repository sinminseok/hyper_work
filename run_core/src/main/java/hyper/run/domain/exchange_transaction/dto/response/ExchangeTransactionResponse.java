package hyper.run.domain.exchange_transaction.dto.response;

import hyper.run.domain.exchange_transaction.entity.ExchangeStatus;
import hyper.run.domain.exchange_transaction.entity.ExchangeTransaction;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ExchangeTransactionResponse {

    private Long exchangeTransactionId;

    private LocalDateTime createdAt;

    private double amount;

    private String accountNumber;

    private String bankName;

    private ExchangeStatus exchangeStatus;

    public static ExchangeTransactionResponse from(ExchangeTransaction exchangeTransaction){
        return ExchangeTransactionResponse.builder()
                .exchangeTransactionId(exchangeTransaction.getId())
                .createdAt(exchangeTransaction.getCreateDateTime())
                .amount(exchangeTransaction.getAmount())
                .exchangeStatus(exchangeTransaction.getExchangeStatus())
                .accountNumber(exchangeTransaction.getAccountNumber())
                .bankName(exchangeTransaction.getBankName())
                .build();
    }
}
