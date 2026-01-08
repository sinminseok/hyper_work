package hyper.run.domain.payment.repository;

import hyper.run.domain.payment.entity.AppleTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppleTransactionRepository extends JpaRepository<AppleTransaction, Long> {

    /**
     * transactionId로 거래 조회 (중복 결제 방지)
     */
    Optional<AppleTransaction> findByTransactionId(String transactionId);

    /**
     * transactionId 존재 여부 확인
     */
    boolean existsByTransactionId(String transactionId);
}
