package hyper.run.domain.inquiry.repository;

import hyper.run.domain.inquiry.entity.CustomerInquiry;
import hyper.run.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerInquiryRepository extends JpaRepository<CustomerInquiry, Long> {
    Optional<CustomerInquiry> findByPaymentId(Long paymentId);
}
