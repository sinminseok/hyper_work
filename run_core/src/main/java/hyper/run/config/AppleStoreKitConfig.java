package hyper.run.config;

import com.apple.itunes.storekit.client.AppStoreServerAPIClient;
import com.apple.itunes.storekit.model.Environment;
import com.apple.itunes.storekit.verification.SignedDataVerifier;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Apple App Store Server API v2 설정
 *
 * .p8 키 파일 로드 우선순위:
 * 1. Classpath (개발 환경)
 * 2. 로컬 파일 시스템
 * 3. S3 (운영 환경)
 */
@Slf4j
@Configuration
public class AppleStoreKitConfig {

    @Value("${apple.store-kit.key-id}")
    private String keyId;

    @Value("${apple.store-kit.issuer-id}")
    private String issuerId;

    @Value("${apple.store-kit.bundle-id}")
    private String bundleId;

    @Value("${apple.store-kit.private-key-path}")
    private String privateKeyPath;

    @Value("${apple.store-kit.environment:SANDBOX}")
    private String environment;

    @Value("${apple.store-kit.app-apple-id:0}")
    private Long appAppleId;

    @Value("${cloud.aws.region.static:ap-northeast-2}")
    private String awsRegion;

    @Bean
    public AppStoreServerAPIClient appStoreServerAPIClient() {
        try {
            String signingKey = loadPrivateKey();
            Environment env = resolveEnvironment();

            AppStoreServerAPIClient client = new AppStoreServerAPIClient(
                    signingKey, keyId, issuerId, bundleId, env
            );

            log.info("AppStoreServerAPIClient 초기화 성공 (environment={})", environment);
            return client;
        } catch (Exception e) {
            log.error("AppStoreServerAPIClient 초기화 실패", e);
            throw new RuntimeException("AppStoreServerAPIClient 초기화 실패: " + e.getMessage(), e);
        }
    }

    @Bean
    public SignedDataVerifier signedDataVerifier() {
        try {
            Set<InputStream> rootCertificates = loadAppleRootCertificates();
            Environment env = resolveEnvironment();

            SignedDataVerifier verifier = new SignedDataVerifier(
                    rootCertificates, bundleId, appAppleId, env, true
            );

            log.info("SignedDataVerifier 초기화 성공 (environment={})", environment);
            return verifier;
        } catch (Exception e) {
            log.error("SignedDataVerifier 초기화 실패", e);
            throw new RuntimeException("SignedDataVerifier 초기화 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Apple Root CA 인증서 로드 (classpath:apple/)
     */
    private Set<InputStream> loadAppleRootCertificates() throws IOException {
        String[] certFiles = {
                "apple/AppleRootCA-G3.cer",
                "apple/AppleIncRootCertificate.cer",
                "apple/AppleRootCA-G2.cer"
        };

        Set<InputStream> certs = new HashSet<>();
        for (String certFile : certFiles) {
            Resource resource = new ClassPathResource(certFile);
            if (!resource.exists()) {
                throw new FileNotFoundException("Apple Root CA 인증서를 찾을 수 없습니다: " + certFile);
            }
            certs.add(resource.getInputStream());
            log.debug("Apple Root CA 인증서 로드: {}", certFile);
        }
        return certs;
    }

    /**
     * .p8 키 파일 로드 (Fallback: classpath -> local -> S3)
     */
    private String loadPrivateKey() throws IOException {
        // 1. Classpath에서 로드 시도
        try {
            String fileName = extractFileName(privateKeyPath);
            Resource resource = new ClassPathResource(fileName);
            if (resource.exists()) {
                log.info("Apple .p8 키 파일 classpath에서 로드: {}", fileName);
                return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.debug("Classpath에서 .p8 키 파일을 찾을 수 없음: {}", e.getMessage());
        }

        // 2. 로컬 파일 시스템에서 로드 시도
        try {
            Path localPath = Paths.get(privateKeyPath);
            if (Files.exists(localPath)) {
                log.info("Apple .p8 키 파일 로컬 파일에서 로드: {}", privateKeyPath);
                return Files.readString(localPath, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.debug("로컬 파일에서 .p8 키 파일을 찾을 수 없음: {}", e.getMessage());
        }

        // 3. S3에서 로드 시도
        if (privateKeyPath.startsWith("s3://")) {
            try {
                log.info("S3에서 .p8 키 파일 로드 시도: {}", privateKeyPath);
                String[] s3Parts = parseS3Path(privateKeyPath);

                AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                        .withRegion(awsRegion)
                        .build();

                S3Object s3Object = s3Client.getObject(s3Parts[0], s3Parts[1]);
                String key = new String(s3Object.getObjectContent().readAllBytes(), StandardCharsets.UTF_8);
                log.info("S3에서 .p8 키 파일 로드 성공");
                return key;
            } catch (Exception e) {
                log.error("S3에서 .p8 키 파일 로드 실패", e);
                throw new IOException("S3에서 .p8 키 파일 로드 실패: " + e.getMessage(), e);
            }
        }

        throw new IOException("Apple .p8 키 파일을 찾을 수 없습니다. " +
                "classpath, 로컬 파일(" + privateKeyPath + "), 또는 S3를 확인하세요.");
    }

    private Environment resolveEnvironment() {
        return "PRODUCTION".equalsIgnoreCase(environment)
                ? Environment.PRODUCTION
                : Environment.SANDBOX;
    }

    private String extractFileName(String path) {
        if (path.contains("/")) {
            return path.substring(path.lastIndexOf('/') + 1);
        }
        return path;
    }

    private String[] parseS3Path(String s3Path) {
        String path = s3Path.substring(5); // "s3://" 제거
        int firstSlash = path.indexOf('/');
        if (firstSlash == -1) {
            throw new IllegalArgumentException("유효하지 않은 S3 경로: " + s3Path);
        }
        return new String[]{path.substring(0, firstSlash), path.substring(firstSlash + 1)};
    }
}
