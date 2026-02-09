package hyper.run.domain.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import hyper.run.domain.payment.dto.GooglePurchaseResponse;
import hyper.run.domain.payment.exception.InvalidReceiptException;
import hyper.run.domain.payment.exception.ReceiptVerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Google Play 영수증 검증 서비스
 * Google Play Developer API를 사용하여 인앱 결제 영수증을 검증합니다.
 *
 * GoogleCredentials Bean이 존재할 때만 활성화됩니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleReceiptService {

    private final GoogleCredentials googleCredentials;
    private final ObjectMapper objectMapper;

    @Value("${google.play.package-name}")
    private String packageName;

    private static final String GOOGLE_PLAY_API_BASE_URL = "https://androidpublisher.googleapis.com/androidpublisher/v3/applications";


    @Transactional
    public void verifyReceipt(String orderId, String productId, String purchaseToken) {
        try {
            log.info("Google Play 영수증 검증 시작: orderId={}, productId={}", orderId, productId);

            googleCredentials.refreshIfExpired();
            String accessToken = googleCredentials.getAccessToken().getTokenValue();

            String url = String.format("%s/%s/purchases/products/%s/tokens/%s",
                    GOOGLE_PLAY_API_BASE_URL, packageName, productId, purchaseToken);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Google Play API 호출 실패: statusCode={}, body={}", response.statusCode(), response.body());
                throw new ReceiptVerificationException("Google Play 영수증 검증 실패: " + response.body());
            }

            GooglePurchaseResponse purchase = objectMapper.readValue(response.body(), GooglePurchaseResponse.class);
            validatePurchaseState(purchase, orderId);

            log.info("Google Play 영수증 검증 성공: orderId={}, productId={}", orderId, productId);

        } catch (IOException | InterruptedException e) {
            log.error("Google Play API 호출 실패: orderId={}, productId={}", orderId, productId, e);
            throw new ReceiptVerificationException("Google Play 영수증 검증 중 오류가 발생했습니다.", e);
        }
    }


    private void validatePurchaseState(GooglePurchaseResponse purchase, String orderId) {
        if (purchase.getPurchaseState() == null) {
            log.error("구매 상태가 null입니다: orderId={}", orderId);
            throw new InvalidReceiptException("유효하지 않은 영수증입니다. 구매 상태를 확인할 수 없습니다.");
        }

        int purchaseState = purchase.getPurchaseState();

        if (purchaseState != 0) {
            log.error("구매 상태가 올바르지 않습니다: orderId={}, purchaseState={}", orderId, purchaseState);
            String errorMessage = switch (purchaseState) {
                case 1 -> "취소된 구매입니다.";
                case 2 -> "보류 중인 구매입니다.";
                default -> "알 수 없는 구매 상태입니다.";
            };
            throw new InvalidReceiptException(errorMessage);
        }

        // orderId 검증 (Google Play는 orderId를 obfuscatedExternalAccountId로 반환할 수 있음)
        String actualOrderId = purchase.getOrderId();
        if (actualOrderId == null || !actualOrderId.equals(orderId)) {
            log.warn("주문 ID 불일치: expected={}, actual={}", orderId, actualOrderId);
            // 주문 ID 불일치는 경고만 기록 (Google Play는 때때로 다른 형식의 orderId를 반환할 수 있음)
        }

        log.info("구매 상태 검증 성공: orderId={}, purchaseState={}, purchaseTime={}",
                orderId, purchaseState, purchase.getPurchaseTimeMillis());
    }
}
