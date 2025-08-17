package hyper.run.domain.exchange_transaction.repository;

import hyper.run.domain.exchange_transaction.entity.ExchangeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangeTransactionRepository extends JpaRepository<ExchangeTransaction, Long> {
    List<ExchangeTransaction> findByUserId(Long userId);

    //ExchangeTransaction 의 상태가 ExchangeStatus.REQUEST 인 값 페이징 조회
}
