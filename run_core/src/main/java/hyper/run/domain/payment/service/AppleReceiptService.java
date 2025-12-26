package hyper.run.domain.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hyper.run.domain.payment.dto.response.AppleTransactionResponse;
import hyper.run.domain.payment.entity.AppleTransaction;
import hyper.run.domain.payment.repository.AppleTransactionRepository;
import hyper.run.exception.custom.DuplicatedTransactionException;
import hyper.run.exception.custom.InvalidReceiptException;
import hyper.run.exception.custom.ReceiptVerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static hyper.run.exception.ErrorMessages.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleReceiptService {

    private final AppleTransactionRepository appleTransactionRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${apple.verify-receipt-url.production:https://buy.itunes.apple.com/verifyReceipt}")
    private String productionUrl;

    @Value("${apple.verify-receipt-url.sandbox:https://sandbox.itunes.apple.com/verifyReceipt}")
    private String sandboxUrl;

    /**
     * Apple 영수증 검증 메서드
     * @param transactionId Apple 거래 ID
     * @param productId 상품 ID
     * @param receiptData Base64로 인코딩된 영수증 데이터 (클라이언트에서 전송)
     */
    @Transactional
    public AppleTransactionResponse verifyReceipt(String transactionId, String productId, String receiptData) {
        // 1. 중복 거래 체크
        if (appleTransactionRepository.existsByTransactionId(transactionId)) {
            log.warn("중복된 Apple 거래 시도: transactionId={}", transactionId);
            throw new DuplicatedTransactionException(DUPLICATED_APPLE_TRANSACTION);
        }

        // 2. Apple 서버에 영수증 검증 요청
        AppleTransactionResponse transactionResponse = callAppleVerifyReceiptAPI(receiptData, transactionId, productId);

        // 3. AppleTransaction 엔티티 생성 및 저장
        AppleTransaction appleTransaction = AppleTransaction.of(transactionResponse);
        appleTransactionRepository.save(appleTransaction);

        log.info("Apple 영수증 검증 성공: transactionId={}, productId={}", transactionId, productId);
        return transactionResponse;
    }

    /**
     * Apple verifyReceipt API 호출
     */
    private AppleTransactionResponse callAppleVerifyReceiptAPI(String receiptData, String transactionId, String productId) {
        try {
            // 요청 바디 구성
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("receipt-data", receiptData);
            requestBody.put("password", ""); // App Store Connect에서 발급받은 Shared Secret (선택사항)
            requestBody.put("exclude-old-transactions", "true");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            // Production 서버 호출
            ResponseEntity<String> response = restTemplate.postForEntity(productionUrl, entity, String.class);

            // 응답 파싱
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            int status = rootNode.get("status").asInt();

            // status 21007: Sandbox 환경 (프로덕션 URL로 샌드박스 영수증 검증 시도한 경우)
            if (status == 21007) {
                log.info("Sandbox 환경 감지, Sandbox URL로 재시도");
                response = restTemplate.postForEntity(sandboxUrl, entity, String.class);
                rootNode = objectMapper.readTree(response.getBody());
                status = rootNode.get("status").asInt();
            }

            // status 0: 성공
            if (status != 0) {
                log.error("Apple 영수증 검증 실패: status={}, transactionId={}", status, transactionId);
                throw new InvalidReceiptException(INVALID_APPLE_RECEIPT + " (status: " + status + ")");
            }

            // 영수증에서 거래 정보 추출
            JsonNode receipt = rootNode.get("receipt");
            JsonNode inApp = receipt.get("in_app");

            // transactionId와 productId 매칭되는 거래 찾기
            for (JsonNode transaction : inApp) {
                String txnId = transaction.get("transaction_id").asText();
                String pId = transaction.get("product_id").asText();

                if (txnId.equals(transactionId) && pId.equals(productId)) {
                    // AppleTransactionResponse 생성
                    return parseTransactionResponse(transaction, rootNode);
                }
            }

            // 매칭되는 거래를 찾지 못한 경우
            log.error("영수증에서 거래를 찾을 수 없음: transactionId={}, productId={}", transactionId, productId);
            throw new InvalidReceiptException(INVALID_APPLE_RECEIPT);

        } catch (DuplicatedTransactionException | InvalidReceiptException e) {
            throw e;
        } catch (Exception e) {
            log.error("Apple 영수증 검증 중 예외 발생: transactionId={}", transactionId, e);
            throw new ReceiptVerificationException(APPLE_RECEIPT_VERIFICATION_FAILED);
        }
    }

    /**
     * JSON 응답을 AppleTransactionResponse로 파싱
     */
    private AppleTransactionResponse parseTransactionResponse(JsonNode transaction, JsonNode rootNode) {
        // Transaction 정보
        String transactionId = transaction.get("transaction_id").asText();
        String productId = transaction.get("product_id").asText();
        long purchaseDateMs = transaction.get("purchase_date_ms").asLong();

        // Environment 정보
        String environment = rootNode.has("environment") ? rootNode.get("environment").asText() : "Production";

        // AppleTransactionResponse 빌더를 사용하여 생성
        return AppleTransactionResponse.builder()
                .transactionId(transactionId)
                .productId(productId)
                .purchaseDate(Instant.ofEpochMilli(purchaseDateMs))
                .environment(environment)
                .status("0") // 성공
                .build();
    }
}
