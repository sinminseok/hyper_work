package hyper.run.domain.payment.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class AppleReceiptService {

    private static final String APPLE_SANDBOX_URL = "https://sandbox.itunes.apple.com/verifyReceipt";
    private static final String APPLE_PRODUCTION_URL = "https://buy.itunes.apple.com/verifyReceipt";
    private static final String SHARED_SECRET = "YOUR_SHARED_SECRET_HERE"; // App Store Connect에서 발급

    public Map<String, Object> verifyReceipt(String base64Receipt) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> request = new HashMap<>();
        request.put("receipt-data", base64Receipt);
        request.put("password", SHARED_SECRET);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                APPLE_PRODUCTION_URL,
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> result = response.getBody();

        // 샌드박스 결제면 다시 샌드박스 URL로 요청
        if (result != null && Objects.equals(result.get("status"), 21007)) {
            response = restTemplate.exchange(
                    APPLE_SANDBOX_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
            result = response.getBody();
        }

        return result;
    }
}