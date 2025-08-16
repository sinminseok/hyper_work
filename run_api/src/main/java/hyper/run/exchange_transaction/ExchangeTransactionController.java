package hyper.run.exchange_transaction;


import hyper.run.domain.exchange_transaction.dto.request.ExchangeTransactionRequest;
import hyper.run.domain.exchange_transaction.dto.response.ExchangeTransactionResponse;
import hyper.run.domain.exchange_transaction.service.ExchangeTransactionService;
import hyper.run.domain.payment.dto.request.PaymentRequest;
import hyper.run.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static hyper.run.auth.service.SecurityContextHelper.getLoginEmailBySecurityContext;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/exchange-transactions")
public class ExchangeTransactionController {

    private final ExchangeTransactionService service;

    @PostMapping
    public ResponseEntity<?> buyCoupons(@RequestBody ExchangeTransactionRequest request) {
        service.save(request);
        SuccessResponse response = new SuccessResponse(true, "환전 신청 성공", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<?> findAll(){
        String email = getLoginEmailBySecurityContext();
        List<ExchangeTransactionResponse> myExchangeHistories = service.findMyExchangeHistories(email);
        SuccessResponse response = new SuccessResponse(true, "내 환전 내역 조회 성공", myExchangeHistories);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
