package hyper.run.api;

import hyper.run.domain.inquiry.dto.request.InquirySearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/admin/inquiry")
@RequiredArgsConstructor
public class CustomerInquiryController {

    @GetMapping
    public ResponseEntity<?> searchInquiry(InquirySearchRequest request, Pageable pageable){

    }
}
