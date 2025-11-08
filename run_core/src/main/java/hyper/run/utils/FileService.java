package hyper.run.utils;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.multipart.MultipartFile;


public interface FileService {
    String toUrls(final MultipartFile file);

    void fileUpload(final MultipartFile file, final String url);

    void deleteFile(final String fileName);
}