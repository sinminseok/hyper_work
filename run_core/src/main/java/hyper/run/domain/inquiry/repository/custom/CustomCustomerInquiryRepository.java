package hyper.run.domain.inquiry.repository.custom;

import hyper.run.domain.inquiry.dto.request.InquirySearchRequest;
import hyper.run.domain.inquiry.dto.response.CustomerInquiryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomCustomerInquiryRepository {
    Page<CustomerInquiryResponse> searchInquiry(InquirySearchRequest inquiryRequest, Pageable pageable);
}
