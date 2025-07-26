package hyper.run.domain.inquiry.repository;

import hyper.run.domain.inquiry.entity.CustomerInquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerInquiryRepository extends JpaRepository<CustomerInquiry, Long> {
}
