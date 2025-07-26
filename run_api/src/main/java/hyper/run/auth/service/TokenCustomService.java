package hyper.run.auth.service;

import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
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
    public void processRefreshToken(String refreshToken, HttpServletResponse response) {
        User user = OptionalUtil.getOrElseThrow(userRepository.findByRefreshToken(refreshToken) , "존재하지 않는 RefreshToken 입니다.");
        String updateRefreshToken = jwtService.createRefreshToken();
        String updateAccessToken = jwtService.createAccessToken(user.getEmail());
        user.setRefreshToken(updateRefreshToken);
        jwtService.sendAccessAndRefreshToken(response, updateAccessToken, updateRefreshToken);
    }
}
