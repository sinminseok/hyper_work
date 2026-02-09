package hyper.run.config;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * Google Play 설정 - Fallback 방식
 *
 * 로드 우선순위:
 * 1. 로컬 파일 (run_core/src/main/resources/funyrun-7d716a03e6f6.json)
 * 2. S3 (s3://hyper-run-image/secrets/google-play-service-account.json)
 *
 * 개발 환경에서는 로컬 파일 사용, 운영 환경에서는 S3 사용
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "google.play.enabled", havingValue = "true", matchIfMissing = false)
public class GooglePlayConfigS3 {

    @Value("${google.play.service-account-key-path}")
    private String keyPath;

    @Value("${google.play.package-name}")
    private String packageName;

    @Value("${cloud.aws.region.static:ap-northeast-2}")
    private String awsRegion;

    @Bean
    public GoogleCredentials googleCredentials() {
        try {
            InputStream inputStream = loadKeyFileWithFallback();

            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(inputStream)
                    .createScoped(Collections.singleton("https://www.googleapis.com/auth/androidpublisher"));

            log.info("Google Credentials 초기화 성공");
            return credentials;

        } catch (IOException e) {
            log.error("Failed to load Google Credentials", e);
            throw new RuntimeException("Google Credentials 초기화 실패: " + e.getMessage(), e);
        }
    }

    @Bean
    public String googlePlayPackageName() {
        return packageName;
    }

    /**
     * Fallback 방식으로 키 파일 로드
     * 1. 로컬 classpath 시도 (개발 환경)
     * 2. 로컬 파일 시스템 시도
     * 3. S3 시도 (운영 환경)
     */
    private InputStream loadKeyFileWithFallback() throws IOException {
        // 1. Classpath에서 로드 시도 (개발 환경)
        try {
            log.info("Classpath에서 키 파일 로드 시도: funyrun-7d716a03e6f6.json");
            Resource resource = new ClassPathResource("funyrun-7d716a03e6f6.json");
            if (resource.exists()) {
                log.info("✅ Classpath에서 키 파일 로드 성공");
                return resource.getInputStream();
            }
        } catch (Exception e) {
            log.debug("Classpath에서 키 파일을 찾을 수 없음: {}", e.getMessage());
        }

        // 2. 로컬 파일 시스템에서 로드 시도
        try {
            Path localPath = Paths.get("run_core/src/main/resources/funyrun-7d716a03e6f6.json");
            if (Files.exists(localPath)) {
                log.info("✅ 로컬 파일에서 키 파일 로드 성공: {}", localPath);
                return new FileInputStream(localPath.toFile());
            }
        } catch (Exception e) {
            log.debug("로컬 파일을 찾을 수 없음: {}", e.getMessage());
        }

        // 3. S3에서 로드 시도 (운영 환경)
        if (keyPath.startsWith("s3://")) {
            try {
                log.info("S3에서 키 파일 로드 시도: {}", keyPath);
                String[] s3Parts = parseS3Path(keyPath);
                String bucketName = s3Parts[0];
                String objectKey = s3Parts[1];

                log.info("S3 Bucket: {}, Key: {}", bucketName, objectKey);

                AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                        .withRegion(awsRegion)
                        .build();

                S3Object s3Object = s3Client.getObject(bucketName, objectKey);
                log.info("✅ S3에서 키 파일 로드 성공");
                return s3Object.getObjectContent();

            } catch (AmazonServiceException e) {
                log.error("S3 접근 실패 ({}): {}", e.getStatusCode(), e.getMessage());
                throw new RuntimeException("S3에서 키 파일 로드 실패: " + e.getMessage(), e);
            }
        }

        throw new IOException("Google Play Service Account 키 파일을 찾을 수 없습니다. " +
                "로컬 파일(funyrun-7d716a03e6f6.json) 또는 S3(" + keyPath + ")를 확인하세요.");
    }

    /**
     * S3 경로를 버킷과 키로 파싱
     */
    private String[] parseS3Path(String s3Path) {
        if (!s3Path.startsWith("s3://")) {
            throw new IllegalArgumentException("S3 경로는 s3://로 시작해야 합니다: " + s3Path);
        }

        String path = s3Path.substring(5);
        int firstSlash = path.indexOf('/');

        if (firstSlash == -1) {
            throw new IllegalArgumentException("유효하지 않은 S3 경로입니다: " + s3Path);
        }

        String bucketName = path.substring(0, firstSlash);
        String objectKey = path.substring(firstSlash + 1);

        return new String[]{bucketName, objectKey};
    }
}
