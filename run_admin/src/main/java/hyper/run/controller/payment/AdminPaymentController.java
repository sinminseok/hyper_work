package hyper.run.controller.payment;

import hyper.run.domain.payment.service.PaymentManagementService;
import hyper.run.dto.payment.AdminPaymentDetailResponse;
import hyper.run.dto.payment.AdminPaymentListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentManagementService paymentManagementService;

    @GetMapping
    public String paymentList(
            @RequestParam(value = "filterStatus", required = false, defaultValue = "ALL") String filterStatus,
            @RequestParam(value = "startDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        Page<AdminPaymentListResponse> payments = paymentManagementService.getPaymentList(
                filterStatus, startDate, endDate, page, size
        );

        model.addAttribute("payments", payments);
        model.addAttribute("filterStatus", filterStatus);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("currentUri", "/admin/payments");

        return "payment/list";
    }

    @GetMapping("/{paymentId}")
    public String paymentDetail(@PathVariable Long paymentId, Model model) {
        AdminPaymentDetailResponse payment = paymentManagementService.getPaymentDetail(paymentId);
        model.addAttribute("payment", payment);
        model.addAttribute("currentUri", "/admin/payments");
        return "payment/detail";
    }

    @PostMapping("/{paymentId}/approve-refund")
    @ResponseBody
    public ResponseEntity<String> approveRefund(
            @PathVariable Long paymentId,
            @RequestParam String reason
    ) {
        paymentManagementService.approveRefund(paymentId, reason);
        return ResponseEntity.ok("환불 승인 완료");
    }

    @PostMapping("/{paymentId}/reject-refund")
    @ResponseBody
    public ResponseEntity<String> rejectRefund(
            @PathVariable Long paymentId,
            @RequestParam String reason
    ) {
        paymentManagementService.rejectRefund(paymentId, reason);
        return ResponseEntity.ok("환불 거절 완료");
    }
}
