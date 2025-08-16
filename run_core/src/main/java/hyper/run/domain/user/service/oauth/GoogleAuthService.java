//package hyper.run.domain.user.service.oauth;
//
//import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
//import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.jackson2.JacksonFactory;
//import hyper.run.domain.user.dto.request.GoogleLoginRequest;
//import hyper.run.exception.custom.NotFoundDataException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.security.GeneralSecurityException;
//import java.util.Collections;
//
//@Service
//@RequiredArgsConstructor
//public class GoogleAuthService {
//
//    @Value("${oauth.android-client-id}")
//    private String androidClientId;
//
//    @Value("${oauth.ios-client-id}")
//    private String iosClientId;
//
//
//    public String getGoogleEmail(GoogleLoginRequest request) throws GeneralSecurityException, IOException {
//        GoogleIdTokenVerifier verifier = createVerifier(request);
//        GoogleIdToken googleIdToken = verifier.verify(request.getIdToken());
//
//        if (googleIdToken == null) {
//            throw new NotFoundDataException("Invalid ID token");
//        }
//
//        return googleIdToken.getPayload().getEmail();
//    }
//
//    private GoogleIdTokenVerifier createVerifier(GoogleLoginRequest request) {
//        String clientId = request.getPlatformType().isAndroid() ? androidClientId : iosClientId;
//        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
//                .setAudience(Collections.singletonList(clientId))
//                .build();
//    }
//
//
//}
