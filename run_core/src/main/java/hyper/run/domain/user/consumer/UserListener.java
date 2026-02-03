package hyper.run.domain.user.consumer;

import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.event.UserCreateEvent;
import hyper.run.domain.user.event.UserDeleteEvent;
import hyper.run.domain.user.event.UserEditEvent;
import hyper.run.domain.user.event.UserProfileImageEvent;
import hyper.run.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import hyper.run.exception.custom.UserDuplicatedException;
import hyper.run.utils.FileService;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static hyper.run.exception.ErrorMessages.NOT_EXIST_USER_EMAIL;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserListener {

    private final UserRepository userRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final FileService fileService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleUserCreated(UserCreateEvent event){
        if(isExistEmail(event.getUser().getEmail())){
            throw new UserDuplicatedException("이미 가입된 이메일입니다.");
        }

        if(isExistPhoneNumber(event.getUser().getPhoneNumber())){
            throw new UserDuplicatedException("이미 가입된 휴대폰 번호입니다.");
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleUserProfileImage(UserProfileImageEvent event){
        User user = OptionalUtil.getOrElseThrow(userRepository.findById(event.getUserId()), NOT_EXIST_USER_EMAIL);
        if (user.isExistProfile()) {
            fileService.deleteFile(user.getProfileUrl());
        }
        user.setProfileUrl(uploadProfileImage(event.getUploadImage()));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleUserEdit(UserEditEvent event){
        User user = OptionalUtil.getOrElseThrow(userRepository.findById(event.getUserId()), NOT_EXIST_USER_EMAIL);
        user.setBirth(event.getBirth());
        user.setName(event.getName());
        user.setPhoneNumber(event.getPhoneNumber());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleUserDelete(UserDeleteEvent event) {
        Long userId = event.userId();

        // 1. 프로필 이미지 삭제 (S3)
        if (event.profileUrl() != null) {
            fileService.deleteFile(event.profileUrl());
        }

        // 2. GameHistory 삭제 (MongoDB)
        gameHistoryRepository.deleteAllByUserId(userId);

        // 3. User 삭제 (Payment, CustomerInquiry는 cascade로 자동 삭제)
        User user = OptionalUtil.getOrElseThrow(userRepository.findById(userId), NOT_EXIST_USER_EMAIL);
        userRepository.delete(user);

        log.info("User deleted. userId: {}", userId);
    }

    private String uploadProfileImage(final MultipartFile image) {
        String url = fileService.toUrls(image);
        fileService.fileUpload(image, url);
        return url;
    }

    private boolean isExistEmail(final String email){
        Optional<User> byEmail = userRepository.findByEmail(email);
        return byEmail.isPresent();
    }

    private boolean isExistPhoneNumber(final String phoneNumber){
        Optional<User> byPhoneNumber = userRepository.findByPhoneNumber(phoneNumber);
        return byPhoneNumber.isPresent();
    }
}
