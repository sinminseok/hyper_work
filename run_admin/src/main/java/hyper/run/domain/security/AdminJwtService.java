package hyper.run.domain.security;

import hyper.run.domain.dto.response.AccessTokenPayload;
import hyper.run.domain.dto.response.LoginResponse;
import hyper.run.domain.dto.response.RefreshTokenPayload;
import hyper.run.domain.entity.AdminUser;
import hyper.run.exception.custom.JwtValidationException;
import hyper.run.utils.SuccessResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
public class AdminJwtService {

    private final SecretKey secretKey;

    @Value("${spring.application.name}")
    private String issuer;

    @Value("${jwt.access.expiration}")
    private long accessKeyExpiration; // 30분

    @Value("${jwt.refresh.expiration}")
    private long refreshKeyExpiration; // 14일

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    public AdminJwtService(@Value("${jwt.secret-key}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    // 커스텀 예외 추가
    public Claims verifyToken(final String token) throws JwtValidationException {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String createAccessToken(final AccessTokenPayload payload){
        return Jwts.builder()
                .setSubject(payload.email())
                .claim("role",payload.role().name())
                .setIssuer(issuer)
                .setIssuedAt(payload.date())
                .setExpiration(new Date(payload.date().getTime()+ accessKeyExpiration * 1000L))
                .signWith(SignatureAlgorithm.HS512,secretKey)
                .compact();
    }
    public String createRefreshToken(final RefreshTokenPayload payload){
        return Jwts.builder()
                .setSubject(payload.email())
                .setIssuer(issuer)
                .setIssuedAt(payload.date())
                .setExpiration(new Date(payload.date().getTime() + refreshKeyExpiration + 1000L))
                .signWith(SignatureAlgorithm.HS512,secretKey)
                .compact();
    }


    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization"))
                .filter(accessToken -> accessToken.startsWith("Bearer "))
                .map(accessToken -> accessToken.replace("Bearer ", ""));
    }
    public void sendAccessAndRefreshToken(HttpServletResponse response,String accessToken,String refreshToken){
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setHeader(accessHeader,accessToken);
        response.setHeader(refreshHeader,refreshToken);
    }

    }
