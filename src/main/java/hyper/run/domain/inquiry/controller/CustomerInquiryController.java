package hyper.run.domain.inquiry.controller;

import hyper.run.domain.inquiry.dto.request.InquiryRequest;
import hyper.run.domain.inquiry.service.CustomerInquiryService;
import hyper.run.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static hyper.run.auth.service.SecurityContextHelper.getLoginEmailBySecurityContext;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/inquiries")
public class CustomerInquiryController {

    private final CustomerInquiryService customerInquiryService;

    //문의 하기 API (환불 신청 or 일반 문의)
    @PostMapping
    public ResponseEntity<?> applyInquiry(@RequestBody InquiryRequest request) {
        String email = getLoginEmailBySecurityContext();
        customerInquiryService.applyInquiry(email, request);
        SuccessResponse response = new SuccessResponse(true, "문의 등록 성공", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
