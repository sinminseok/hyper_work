package hyper.run.domain.inquiry.service;

import hyper.run.domain.inquiry.dto.request.InquirySearchRequest;
import hyper.run.domain.inquiry.dto.response.CustomerInquiryResponse;
import hyper.run.domain.inquiry.entity.CustomerInquiry;
import hyper.run.domain.inquiry.entity.InquiryState;
import hyper.run.domain.inquiry.repository.CustomerInquiryRepository;
import hyper.run.domain.inquiry.repository.custom.CustomCustomerInquiryRepository;

import hyper.run.utils.EmailService;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminCustomerInquiryService {

    private final CustomerInquiryRepository repository;
    private final CustomCustomerInquiryRepository customerInquiryRepository;
    private final EmailService emailService;

    /** 관리자
     * 문의사항 필터 조회
     */
    public Page<CustomerInquiryResponse> searchInquiry(InquirySearchRequest request, Pageable pageable){
        return customerInquiryRepository.searchInquiry(request,pageable);
    }

    public void deleteInquiry(Long inquiryId){
        CustomerInquiry inquiry = OptionalUtil.getOrElseThrow(repository.findById(inquiryId),"존재하지 않는 문의사항입니다.");
        repository.delete(inquiry);
    }

    /**
     * 문의사항 답변
     */
    @Transactional
    public void answerInquiry(Long inquiryId,String answer){
        CustomerInquiry customerInquiry = OptionalUtil.getOrElseThrow(repository.findById(inquiryId),"존재하지 않는 문의사항입니다.");
        customerInquiry.setState(InquiryState.SUCCESS);
        customerInquiry.setAnswer(answer);
        emailService.sendSimpleEmail(customerInquiry.getEmail(),"문의사항 답변 안내입니다." ,answer);
    }
}
