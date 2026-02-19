package hyper.run.domain.user.service;

import hyper.run.domain.user.dto.request.UserSignupRequest;
import hyper.run.domain.user.dto.request.UserUpdateRequest;
import hyper.run.domain.user.dto.request.UserWatchRegisterRequest;
import hyper.run.domain.user.dto.response.UserProfileResponse;
import hyper.run.domain.user.dto.response.UserVerifyResponse;
import hyper.run.domain.user.dto.response.UserWatchConnectedResponse;
import hyper.run.domain.user.dto.response.UserWatchResponse;
import hyper.run.domain.user.dto.response.WatchTokenResponse;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.entity.UserWatch;
import hyper.run.domain.user.event.UserDeleteEvent;
import hyper.run.domain.user.event.UserEditEvent;
import hyper.run.domain.user.event.UserProfileImageEvent;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.domain.user.repository.UserWatchRepository;
import hyper.run.exception.custom.UserDuplicatedException;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static hyper.run.exception.ErrorMessages.ALREADY_EXIST_EMAIL;
import static hyper.run.exception.ErrorMessages.NOT_EXIST_USER_EMAIL;
import static hyper.run.exception.ErrorMessages.NOT_EXIST_USER_WATCH;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final String NONE = "NONE";

    private final UserRepository userRepository;
    private final UserWatchRepository userWatchRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void save(final UserSignupRequest userSignupRequest, final String encodePassword) {
        if (isExistEmail(userSignupRequest.getEmail())) {
            throw new UserDuplicatedException(ALREADY_EXIST_EMAIL);
        }

        User user = userSignupRequest.toEntity(encodePassword);
        userRepository.save(user);
    }


    @Transactional
    public void updateImage(Long userId, MultipartFile image){
        eventPublisher.publishEvent(new UserProfileImageEvent(userId, image));
    }

    @Transactional
    public void editPassword(final String email, final String encodePassword) {
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        user.updatePassword(encodePassword);
    }

    public boolean isExistEmail(final String email){
        Optional<User> byEmail = userRepository.findByEmail(email);
        return byEmail.isPresent();
    }

    public boolean isExistPhoneNumber(final String phoneNumber){
        Optional<User> byPhoneNumber = userRepository.findByPhoneNumber(phoneNumber);
        return byPhoneNumber.isPresent();
    }

    public String findEmailByPhoneNumber(final String phoneNumber){
        Optional<User> byPhoneNumber = userRepository.findByPhoneNumber(phoneNumber);
        if(byPhoneNumber.isPresent()){
            return byPhoneNumber.get().getEmail();
        }
        return NONE;
    }

    public UserProfileResponse getMyProfile(final String email) {
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        return UserProfileResponse.toProfileResponse(user);
    }

    public int getMyCouponAmount(final String email){
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        return user.getCoupon();
    }

    @Transactional
    public void deleteUser(final String email){
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        eventPublisher.publishEvent(UserDeleteEvent.from(user.getId(), user.getProfileUrl()));
    }

    @Transactional
    public void updateProfile(Long userId, UserUpdateRequest userUpdateRequest) {
        eventPublisher.publishEvent(new UserEditEvent(userId,
                userUpdateRequest.getName(),
                userUpdateRequest.getPhoneNumber(),
                userUpdateRequest.getBirth()));
    }

    public UserWatchConnectedResponse findUserWatchConnectedResponse(final String email){
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        return UserWatchConnectedResponse.from(user);
    }

    public String getEmailByWatchKey(final String watchKey) {
        User user = OptionalUtil.getOrElseThrow(userRepository.findByWatchConnectedKey(watchKey), NOT_EXIST_USER_EMAIL);
        return user.getEmail();
    }

    @Transactional
    public WatchTokenResponse saveWatchRefreshToken(final String email, final String accessToken, final String refreshToken) {
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        user.setWatchRefreshToken(refreshToken);
        return WatchTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public String getHashedPassword(final String email) {
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        return user.getPassword();
    }

    public UserVerifyResponse verifyUser(final String loginEmail, final String inputEmail, final boolean passwordMatched) {
        boolean emailMatched = loginEmail.equals(inputEmail);

        if (emailMatched && passwordMatched) {
            return UserVerifyResponse.success();
        } else if (!emailMatched && !passwordMatched) {
            return UserVerifyResponse.bothMismatch();
        } else if (!emailMatched) {
            return UserVerifyResponse.emailMismatch();
        } else {
            return UserVerifyResponse.passwordMismatch();
        }
    }

    @Transactional
    public void registerUserWatch(Long userId, UserWatchRegisterRequest request) {
        UserWatch userWatch = request.toEntity(userId);
        userWatchRepository.save(userWatch);
    }

    public UserWatchResponse getUserWatch(Long userId) {
        UserWatch userWatch = OptionalUtil.getOrElseThrow(userWatchRepository.findByUserId(userId), NOT_EXIST_USER_WATCH);
        return UserWatchResponse.from(userWatch);
    }
}
