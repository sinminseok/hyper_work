package hyper.run.payment;


import hyper.run.domain.payment.dto.request.PaymentRequest;
import hyper.run.domain.payment.dto.response.PaymentResponse;
import hyper.run.domain.payment.service.PaymentService;
import hyper.run.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static hyper.run.auth.service.SecurityContextHelper.getLoginEmailBySecurityContext;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제(쿠폰 구매) API
     */
    @PostMapping
    public ResponseEntity<?> buyCoupons(@RequestBody PaymentRequest request) {
        String email = getLoginEmailBySecurityContext();
        paymentService.pay(email, request);
        SuccessResponse response = new SuccessResponse(true, "결제 성공", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<?> findAll() {
        String email = getLoginEmailBySecurityContext();
        List<PaymentResponse> allByEmail = paymentService.findAllByEmail(email);
        SuccessResponse response = new SuccessResponse(true, "내 결제 내역 조회", allByEmail);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 환불 가능한 결제 내역 조회 API
     */
    @GetMapping("/refunds")
    public ResponseEntity<?> applyInquiry( ) {
        String email = getLoginEmailBySecurityContext();
        List<PaymentResponse> possibleRefundPayment = paymentService.findPossibleRefundPayment(email);
        SuccessResponse response = new SuccessResponse(true, "환불 가능한 결제 내역 조회 성공", possibleRefundPayment);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
