package hyper.run.api;

import hyper.run.domain.inquiry.dto.request.InquirySearchRequest;
import hyper.run.domain.inquiry.dto.response.CustomerInquiryResponse;
import hyper.run.domain.inquiry.repository.custom.CustomCustomerInquiryRepository;
import hyper.run.domain.inquiry.service.CustomerInquiryService;
import hyper.run.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/admin/inquiry")
@RequiredArgsConstructor
public class CustomerInquiryController {

    private final CustomerInquiryService inquiryService;

    @GetMapping
    public ResponseEntity<?> searchInquiry(@ModelAttribute InquirySearchRequest request,
                                           @PageableDefault(size = 6) Pageable pageable){
        Page<CustomerInquiryResponse> inquiries = inquiryService.searchInquiry(request,pageable);
        SuccessResponse response = new SuccessResponse(true,"조건별 문의사항 조회 성공",inquiries);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/answer")
    public ResponseEntity<?> answerInquiry(@PathVariable Long inquiryId,@RequestParam String answer){
        inquiryService.answerInquiry(inquiryId,answer);
        SuccessResponse response = new SuccessResponse(true,"답변 작성 성공",null);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
}
