package hyper.run.domain.user.service;

import hyper.run.domain.user.dto.request.AligoProperties;
import hyper.run.utils.AuthCodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * 문자 전송 서비스(알리고)
 */
@Service
@RequiredArgsConstructor
public class SmsService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final AligoProperties aligoProperties;

    @Async
    public void sendSms(final String phoneNumber, final String code) {
        String url = "https://apis.aligo.in/send/";
        String message = "[HYPER.RUN] 인증번호는 " + code + " 입니다.";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("key", aligoProperties.getApiKey());
        params.add("user_id", aligoProperties.getUserId());
        params.add("sender", aligoProperties.getSender());
        params.add("receiver", phoneNumber);
        params.add("msg", message);
        params.add("msg_type", "SMS");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("SMS 전송 결과: " + response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("SMS 전송 실패");
        }
    }
}