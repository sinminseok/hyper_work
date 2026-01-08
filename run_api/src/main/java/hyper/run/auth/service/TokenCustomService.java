package hyper.run.auth.service;

import hyper.run.auth.dto.LoginResponse;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class TokenCustomService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Transactional
    public LoginResponse processRefreshToken(String refreshToken) {
        // 모바일 RefreshToken으로 먼저 조회
        Optional<User> userByMobileToken = userRepository.findByRefreshToken(refreshToken);

        if (userByMobileToken.isPresent()) {
            // 모바일 토큰 갱신
            User user = userByMobileToken.get();
            String updateRefreshToken = jwtService.createRefreshToken();
            String updateAccessToken = jwtService.createAccessToken(user.getEmail());
            user.setRefreshToken(updateRefreshToken);

            return LoginResponse.builder()
                    .accessToken(updateAccessToken)
                    .refreshToken(updateRefreshToken)
                    .build();
        }

        // 워치 RefreshToken으로 조회
        User user = OptionalUtil.getOrElseThrow(
            userRepository.findByWatchRefreshToken(refreshToken),
            "존재하지 않는 Watch RefreshToken 입니다."
        );

        // 워치 토큰 갱신
        String updateRefreshToken = jwtService.createRefreshToken();
        String updateAccessToken = jwtService.createAccessToken(user.getEmail());
        user.setWatchRefreshToken(updateRefreshToken);

        return LoginResponse.builder()
                .accessToken(updateAccessToken)
                .refreshToken(updateRefreshToken)
                .build();
    }
}
