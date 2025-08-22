package hyper.run.domain.exchange_transaction.dto.response;

import hyper.run.domain.exchange_transaction.entity.ExchangeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AdminExchangeTransactionResponse {
    private Long id;
    private Long userId;
    private String name;
    private Double amount;
    private String accountNumber;
    private String bankName;
    private ExchangeStatus exchangeStatus;
    private LocalDateTime createDateTime;

    public AdminExchangeTransactionResponse(Long id, Long userId, double amount, String accountNumber, String bankName, ExchangeStatus exchangeStatus, String name,LocalDateTime createDateTime) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.exchangeStatus = exchangeStatus;
        this.name = name;
        this.createDateTime = createDateTime;
    }
}
