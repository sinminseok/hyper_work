package hyper.run.domain.user.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthCodeService {

    private final RedisTemplate<String, String> redisTemplate;

    public void saveAuthCode(String phoneNumber, String code) {
        redisTemplate.opsForValue().set("authCode:" + phoneNumber, code, Duration.ofMinutes(3));
    }

    public boolean verifyCode(String phoneNumber, String inputCode) {
        String key = "authCode:" + phoneNumber;
        String savedCode = redisTemplate.opsForValue().get(key);
        return savedCode != null && savedCode.equals(inputCode);
    }
}
