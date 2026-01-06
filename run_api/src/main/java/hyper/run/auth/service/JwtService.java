package hyper.run.auth.service;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import hyper.run.auth.dto.LoginResponse;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.exception.ErrorResponseCode;
import hyper.run.exception.custom.AuthException;
import hyper.run.utils.SuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static hyper.run.auth.constants.AuthConstants.*;

@Service
@RequiredArgsConstructor
@Getter
@Slf4j
public class JwtService {
    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;


    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public String createAccessToken(String email) {
        return JWT.create()
                .withSubject(ACCESS_TOKEN_SUBJECT)
                .withClaim(EMAIL_CLAIM, email)
                .withClaim("uuid", UUID.randomUUID().toString()) // <- 매번 다른 값 추가
                .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpirationPeriod))
                .sign(Algorithm.HMAC512(secretKey));
    }

    public String createRefreshToken() {
        return JWT.create()
                .withSubject(REFRESH_TOKEN_SUBJECT)
                .withClaim("uuid", UUID.randomUUID().toString()) // <- 매번 다른 값
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpirationPeriod))
                .sign(Algorithm.HMAC512(secretKey));
    }

    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        SuccessResponse successResponse = new SuccessResponse(true, SUCCESS_GENERATED_TOKEN, loginResponse);
        try {
            String responseBody = objectMapper.writeValueAsString(successResponse);
            response.getWriter().write(responseBody);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            throw new AuthException(ErrorResponseCode.NOT_VALID_TOKEN, FAIL_TO_TRANSFER_TOKEN);
        }

    }


    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }


    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(accessToken -> accessToken.startsWith(BEARER))
                .map(accessToken -> accessToken.replace(BEARER, ""));
    }


    public Optional<String> extractEmail(String accessToken) {
        try {
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secretKey))
                    .build()
                    .verify(accessToken)
                    .getClaim(EMAIL_CLAIM)
                    .asString());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void isTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(secretKey)).build().verify(token);
        } catch (com.auth0.jwt.exceptions.TokenExpiredException e) {
            throw new AuthException(ErrorResponseCode.NOT_VALID_TOKEN, "토큰이 만료되었습니다.");
        } catch (com.auth0.jwt.exceptions.SignatureVerificationException e) {
            throw new AuthException(ErrorResponseCode.NOT_VALID_TOKEN, "토큰 서명이 유효하지 않습니다.");
        } catch (Exception e) {
            throw new AuthException(ErrorResponseCode.NOT_VALID_TOKEN, "유효하지 않은 토큰입니다.");
        }
    }
}
