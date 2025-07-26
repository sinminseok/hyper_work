package hyper.run.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AppleAuthService {
//
//    @Value("${apple.key-url}")
//    private String keyUrl;
//
//    public String getAppleEmail(String identityToken) throws ParseException, JOSEException, IOException {
//        JWKSet jwkSet = loadAppleJWKSet();
//        JWSObject jwsObject = parseIdentityToken(identityToken);
//        JWK jwk = getJwk(jwsObject, jwkSet);
//        JWSVerifier verifier = createJWSVerifier(jwk);
//
//        if (!verifyToken(jwsObject, verifier)) {
//            throw new JOSEException("Token verification failed");
//        }
//
//        return extractEmailFromPayload(jwsObject);
//    }
//
//    private JWKSet loadAppleJWKSet() throws IOException, ParseException {
//        return JWKSet.load(new URL(keyUrl));
//    }
//
//    private JWSObject parseIdentityToken(String identityToken) throws ParseException {
//        return JWSObject.parse(identityToken);
//    }
//
//    private JWK getJwk(JWSObject jwsObject, JWKSet jwkSet) {
//        return jwkSet.getKeyByKeyId(jwsObject.getHeader().getKeyID());
//    }
//
//    private JWSVerifier createJWSVerifier(JWK jwk) throws JOSEException {
//        RSAKey rsaKey = jwk.toRSAKey();
//        return new RSASSAVerifier(rsaKey);
//    }
//
//    private boolean verifyToken(JWSObject jwsObject, JWSVerifier verifier) throws JOSEException {
//        return jwsObject.verify(verifier);
//    }
//
//    private String extractEmailFromPayload(JWSObject jwsObject) {
//        Map<String, Object> payload = jwsObject.getPayload().toJSONObject();
//        return (String) payload.get("email");
//    }
}