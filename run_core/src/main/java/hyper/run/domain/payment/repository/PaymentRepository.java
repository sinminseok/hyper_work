package hyper.run.domain.payment.repository;

import hyper.run.domain.payment.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    List<Payment> findByUserId(Long userId);

    /**
     * Payment 조회 및 PESSIMISTIC_WRITE Lock 획득
     * - 환불 처리 시 동시성 제어를 위해 사용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :paymentId")
    Optional<Payment> findByIdForUpdate(@Param("paymentId") Long paymentId);

    /**
     * transactionId로 결제 내역 존재 여부 확인
     * - 중복 결제 방지를 위해 사용
     */
    boolean existsByTransactionId(String transactionId);

    /**
     * transactionId로 결제 내역 조회 및 PESSIMISTIC_WRITE Lock 획득
     * - 중복 결제 방지를 위한 동시성 제어에 사용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.transactionId = :transactionId")
    Optional<Payment> findByTransactionIdForUpdate(@Param("transactionId") String transactionId);
}
