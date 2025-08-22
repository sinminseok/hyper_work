package hyper.run.api;

import hyper.run.domain.payment.dto.request.PaymentSearchRequest;
import hyper.run.domain.payment.dto.response.AdminPaymentResponse;
import hyper.run.domain.inquiry.dto.response.RefundPaymentResponse;
import hyper.run.domain.payment.service.AdminPaymentService;
import hyper.run.domain.payment.service.PaymentService;
import hyper.run.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/admin/payments")
public class AdminPaymentController {

    private final AdminPaymentService paymentService;

    @GetMapping
    public ResponseEntity<?> getPayments(@ModelAttribute PaymentSearchRequest searchRequest,
                                        @PageableDefault(size = 6) Pageable pageable){
        Page<AdminPaymentResponse> payments = paymentService.searchPayments(searchRequest,pageable);
        SuccessResponse response = new SuccessResponse(true,"조건별 결제 조회 성공",payments);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/refunds/{paymentId}")
    public ResponseEntity<?> getRefundPayment(@PathVariable Long paymentId){
        RefundPaymentResponse payment = paymentService.getRefundPayment(paymentId);
        SuccessResponse response = new SuccessResponse(true,"환불요청 조회 성공",payment);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
    // 환불 요청 승인 api
    @PostMapping("/refunds/{paymentId}/confirm")
    public ResponseEntity<?> confirmRefund(@PathVariable Long paymentId){
        paymentService.confirmRefund(paymentId);
        SuccessResponse response = new SuccessResponse(true,"환불 승인 성공",null);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PostMapping("/refunds/{paymentId}/reject")
    public ResponseEntity<?> rejectRefund(@PathVariable Long paymentId){
        paymentService.rejectRefund(paymentId);
        SuccessResponse response = new SuccessResponse(true,"환불 거절 성공",null);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
}
