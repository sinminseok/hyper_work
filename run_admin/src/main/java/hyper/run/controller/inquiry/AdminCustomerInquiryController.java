package hyper.run.controller.inquiry;

import hyper.run.domain.inquiry.service.CustomerInquiryManagementService;
import hyper.run.dto.inquiry.AdminCustomerInquiryDetailResponse;
import hyper.run.dto.inquiry.AdminCustomerInquiryListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/support")
@RequiredArgsConstructor
public class AdminCustomerInquiryController {

    private final CustomerInquiryManagementService customerInquiryManagementService;

    @GetMapping
    public String inquiryList(
            @RequestParam(value = "filterState", required = false, defaultValue = "ALL") String filterState,
            @RequestParam(value = "filterType", required = false, defaultValue = "ALL") String filterType,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "sortBy", required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        Page<AdminCustomerInquiryListResponse> inquiries = customerInquiryManagementService.getInquiryList(
                filterState, filterType, startDate, endDate, sortBy, page, size
        );

        model.addAttribute("inquiries", inquiries);
        model.addAttribute("filterState", filterState);
        model.addAttribute("filterType", filterType);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("currentUri", "/admin/support");

        return "inquiry/list";
    }

    @GetMapping("/{inquiryId}")
    public String inquiryDetail(@PathVariable Long inquiryId, Model model) {
        AdminCustomerInquiryDetailResponse inquiry = customerInquiryManagementService.getInquiryDetail(inquiryId);
        model.addAttribute("inquiry", inquiry);
        model.addAttribute("currentUri", "/admin/support");

        // REFUND 타입인 경우 환불 처리 페이지로 리다이렉트
        if (inquiry.getType().name().equals("REFUND")) {
            return "inquiry/refund-detail";
        }

        return "inquiry/detail";
    }

    @PostMapping("/{inquiryId}/answer")
    @ResponseBody
    public ResponseEntity<String> sendAnswer(
            @PathVariable Long inquiryId,
            @RequestParam String answer
    ) {
        customerInquiryManagementService.sendAnswer(inquiryId, answer);
        return ResponseEntity.ok("답변이 전송되었습니다.");
    }

    @PostMapping("/{inquiryId}/delete")
    @ResponseBody
    public ResponseEntity<String> deleteInquiry(@PathVariable Long inquiryId) {
        customerInquiryManagementService.deleteInquiry(inquiryId);
        return ResponseEntity.ok("문의가 삭제되었습니다.");
    }

    @PostMapping("/{inquiryId}/refund/approve")
    @ResponseBody
    public ResponseEntity<String> approveRefund(
            @PathVariable Long inquiryId,
            @RequestParam String reason
    ) {
        customerInquiryManagementService.approveRefund(inquiryId, reason);
        return ResponseEntity.ok("환불이 승인되었습니다.");
    }

    @PostMapping("/{inquiryId}/refund/reject")
    @ResponseBody
    public ResponseEntity<String> rejectRefund(
            @PathVariable Long inquiryId,
            @RequestParam String reason
    ) {
        customerInquiryManagementService.rejectRefund(inquiryId, reason);
        return ResponseEntity.ok("환불이 거절되었습니다.");
    }
}
