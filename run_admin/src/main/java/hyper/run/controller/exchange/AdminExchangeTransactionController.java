package hyper.run.controller.exchange;

import hyper.run.domain.exchange_transaction.service.ExchangeTransactionManagementService;
import hyper.run.dto.exchange.AdminExchangeTransactionDetailResponse;
import hyper.run.dto.exchange.AdminExchangeTransactionListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/exchanges")
@RequiredArgsConstructor
public class AdminExchangeTransactionController {

    private final ExchangeTransactionManagementService exchangeTransactionManagementService;

    @GetMapping
    public String exchangeTransactionList(
            @RequestParam(value = "filterStatus", required = false, defaultValue = "ALL") String filterStatus,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        Page<AdminExchangeTransactionListResponse> exchanges = exchangeTransactionManagementService.getExchangeTransactionList(
                filterStatus, startDate, endDate, page, size
        );

        model.addAttribute("exchanges", exchanges);
        model.addAttribute("filterStatus", filterStatus);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("currentUri", "/exchanges");

        return "exchange/list";
    }

    @GetMapping("/{transactionId}")
    public String exchangeTransactionDetail(@PathVariable Long transactionId, Model model) {
        AdminExchangeTransactionDetailResponse exchange = exchangeTransactionManagementService.getExchangeTransactionDetail(transactionId);
        model.addAttribute("exchange", exchange);
        model.addAttribute("currentUri", "/exchanges");
        return "exchange/detail";
    }

    @PostMapping("/{transactionId}/approve")
    @ResponseBody
    public ResponseEntity<String> approveExchange(@PathVariable Long transactionId) {
        exchangeTransactionManagementService.approveExchange(transactionId);
        return ResponseEntity.ok("환전 승인 완료");
    }

    @PostMapping("/{transactionId}/reject")
    @ResponseBody
    public ResponseEntity<String> rejectExchange(@PathVariable Long transactionId) {
        exchangeTransactionManagementService.rejectExchange(transactionId);
        return ResponseEntity.ok("환전 거절 완료");
    }
}
