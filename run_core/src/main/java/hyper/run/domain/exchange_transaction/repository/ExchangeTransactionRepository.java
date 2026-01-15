package hyper.run.domain.exchange_transaction.repository;

import hyper.run.domain.exchange_transaction.entity.ExchangeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangeTransactionRepository extends JpaRepository<ExchangeTransaction, Long>, JpaSpecificationExecutor<ExchangeTransaction> {
    List<ExchangeTransaction> findByUserId(Long userId);
}
