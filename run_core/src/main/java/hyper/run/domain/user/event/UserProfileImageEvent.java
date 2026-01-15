package hyper.run.domain.user.event;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class UserProfileImageEvent {
    private final Long userId;
    private final MultipartFile uploadImage;

    public UserProfileImageEvent(Long userId, MultipartFile uploadImage) {
        this.userId = userId;
        this.uploadImage = uploadImage;
    }
}
