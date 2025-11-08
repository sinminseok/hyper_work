package hyper.run.utils;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.multipart.MultipartFile;

@ConditionalOnProperty(prefix = "cloud.aws.s3", name = "enabled", havingValue = "true")
public interface FileService {
    String toUrls(final MultipartFile file);

    void fileUpload(final MultipartFile file, final String url);

    void deleteFile(final String fileName);
}