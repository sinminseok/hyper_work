package hyper.run.domain.user.service.oauth;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class NaverAuthService {

    public String getNaverEmail(String accessToken) {
        String url = "https://openapi.naver.com/v1/nid/me";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("response")) {
                Map<String, Object> responseData = (Map<String, Object>) responseBody.get("response");
                return (String) responseData.get("email");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
