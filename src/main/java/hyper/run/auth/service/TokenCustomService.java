package hyper.run.auth.service;

import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class TokenCustomService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Transactional
    public void processRefreshToken(String refreshToken, HttpServletResponse response) throws IllegalAccessException {
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalAccessException("refresh token 인증 실패"));
        String updateRefreshToken = jwtService.createRefreshToken();
        String updateAccessToken = jwtService.createAccessToken(user.getEmail());
        user.setRefreshToken(updateRefreshToken);
        jwtService.sendAccessAndRefreshToken(response, updateAccessToken, updateRefreshToken);
    }
}
