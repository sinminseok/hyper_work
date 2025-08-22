package hyper.run.domain.exchange_transaction.repository.admin;

import hyper.run.domain.exchange_transaction.dto.response.AdminExchangeTransactionResponse;
import hyper.run.domain.exchange_transaction.entity.ExchangeStatus;
import hyper.run.domain.exchange_transaction.entity.ExchangeTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface CustomExchangeTransactionRepository {
    Page<AdminExchangeTransactionResponse> findExchanges(LocalDate startDate, LocalDate endDate, String keyword, ExchangeStatus exchangeStatus, Pageable pageable);
}
