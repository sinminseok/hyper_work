package hyper.run.api;

import hyper.run.domain.exchange_transaction.dto.response.AdminExchangeTransactionResponse;
import hyper.run.domain.exchange_transaction.entity.ExchangeStatus;
import hyper.run.domain.exchange_transaction.service.AdminExchangeTransactionService;
import hyper.run.domain.exchange_transaction.service.ExchangeTransactionService;
import hyper.run.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@RestController
@RequestMapping("/v1/api/admin/exchanges")
@RequiredArgsConstructor
public class AdminExchangeTransactionController {

    private final AdminExchangeTransactionService exchangeTransactionService;

    @GetMapping
    public ResponseEntity<?> getExchanges(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) ExchangeStatus exchangeStatus,
                                          @PageableDefault(size = 6) Pageable pageable){
        Page<AdminExchangeTransactionResponse> pages = exchangeTransactionService.findExchanges(startDate,endDate,keyword,exchangeStatus,pageable);
        SuccessResponse response = new SuccessResponse(true,"환전 조회 성공",pages);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PatchMapping("/confirm/{id}")
    public ResponseEntity<?> confirmExchange(@PathVariable Long id){
        exchangeTransactionService.complete(id);
        SuccessResponse response = new SuccessResponse(true,"환전 승인완료",null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/cancel/{id}")
    public ResponseEntity<?> cancelExchange(@PathVariable Long id){
        exchangeTransactionService.cancel(id);
        SuccessResponse response = new SuccessResponse(true,"환전 취소완료",null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
