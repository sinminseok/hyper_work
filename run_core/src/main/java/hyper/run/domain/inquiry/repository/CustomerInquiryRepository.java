package hyper.run.domain.inquiry.repository;

import hyper.run.domain.inquiry.entity.CustomerInquiry;
import hyper.run.domain.inquiry.entity.InquiryState;
import hyper.run.domain.inquiry.entity.InquiryType;
import hyper.run.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerInquiryRepository extends JpaRepository<CustomerInquiry, Long>, JpaSpecificationExecutor<CustomerInquiry> {
    Optional<CustomerInquiry> findByPaymentId(Long paymentId);

    // 특정 결제의 환불 요청 조회 (type=REFUND, state=WAITING)
    Optional<CustomerInquiry> findByPaymentIdAndTypeAndState(Long paymentId, InquiryType type, InquiryState state);

    // 여러 결제의 환불 요청 조회
    List<CustomerInquiry> findByPaymentIdInAndTypeAndState(List<Long> paymentIds, InquiryType type, InquiryState state);
}
