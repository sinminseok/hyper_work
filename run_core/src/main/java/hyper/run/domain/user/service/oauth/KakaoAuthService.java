package hyper.run.domain.user.service.oauth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hyper.run.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static hyper.run.utils.EmailConverter.toKaKaoEmail;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getKaKaoEmail(String accessToken) {
        // 1. 카카오 사용자 정보 조회
        RestTemplate restTemplate = new RestTemplate();
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

        org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                userInfoUrl,
                org.springframework.http.HttpMethod.GET,
                entity,
                String.class
        );

        try {
            JsonNode userInfo = objectMapper.readTree(response.getBody());
            Long kakaoId = userInfo.get("id").asLong();
            return toKaKaoEmail(kakaoId.toString());
        } catch (Exception e) {
            throw new RuntimeException("카카오 사용자 정보 파싱 실패", e);
        }
    }
}
