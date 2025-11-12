package hyper.run.domain.user.service;

import hyper.run.domain.payment.dto.response.PaymentResponse;
import hyper.run.domain.user.dto.request.UserSignupRequest;
import hyper.run.domain.user.dto.request.UserUpdateRequest;
import hyper.run.domain.user.dto.response.UserAdminResponse;
import hyper.run.domain.user.dto.response.UserProfileResponse;
import hyper.run.domain.user.dto.response.UserWatchConnectedResponse;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.exception.custom.UserDuplicatedException;
import hyper.run.utils.FileService;
import hyper.run.utils.OptionalUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static hyper.run.exception.ErrorMessages.NOT_EXIST_USER_EMAIL;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final String NONE = "NONE";

    private final UserRepository userRepository;
    private final FileService fileService;

    @Transactional
    public void save(final UserSignupRequest userSignupRequest, final String encodePassword) {
        validateDuplicatedEmail(userSignupRequest.getEmail());
        validateDuplicatedPhoneNumber(userSignupRequest.getPhoneNumber());
        User user = userSignupRequest.toEntity(encodePassword);
        userRepository.save(user);
    }

    @Transactional
    public void updateImage(String email, MultipartFile image){
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        if (user.isExistProfile()) {
            fileService.deleteFile(user.getProfileUrl());
        }

        user.setProfileUrl(uploadProfileImage(image));
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

    @Transactional
    public void chargeCoupon(final String email, final int amount){
        //사용자가 실제로 결제했는지를 검증하는 메서드 만들면 좋을듯함
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        user.chargeCoupon(amount);
    }

    /**
     * 휴대폰번호로 가입된 이메일(아이디) 찾는 메서드
     */
    public String findEmailByPhoneNumber(final String phoneNumber){
        Optional<User> byPhoneNumber = userRepository.findByPhoneNumber(phoneNumber);
        if(byPhoneNumber.isPresent()){
            return byPhoneNumber.get().getEmail();
        }
        return NONE;
    }

    /**
     * 내 프로필 조회 메서드
     */
    public UserProfileResponse getMyProfile(final String email) {
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        return UserProfileResponse.toProfileResponse(user);
    }

    /**
     * 내 잔여 쿠폰 조회 메서드
     */
    public int getMyCouponAmount(final String email){
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        return user.getCoupon();
    }

    /**
     * 자신의 모든 결제 내역을 조회하는 메서드
     */
    public List<PaymentResponse> getMyPayments(final String email){
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        return user.getPayments().stream()
                .map(p->PaymentResponse.toResponse(p))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(final String email){
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        userRepository.delete(user);
    }

    @Transactional
    public void updateProfile(String email, UserUpdateRequest userUpdateRequest) {
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        user.setBirth(userUpdateRequest.getBirth());
        user.setName(userUpdateRequest.getName());
        user.setPhoneNumber(userUpdateRequest.getPhoneNumber());
    }

    public UserWatchConnectedResponse findUserWatchConnectedResponse(final String email){
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        return UserWatchConnectedResponse.from(user);
    }

    public String checkWatchKey(final String watchKey) {
        User user = OptionalUtil.getOrElseThrow(userRepository.findByWatchConnectedKey(watchKey), NOT_EXIST_USER_EMAIL);
        return user.getAccessToken();
    }


    private void validateDuplicatedEmail(String email){
        if(isExistEmail(email)){
            throw new UserDuplicatedException("이미 가입된 이메일입니다.");
        }
    }

    private void validateDuplicatedPhoneNumber(String number){
        if(isExistPhoneNumber(number)){
            throw new UserDuplicatedException("이미 가입된 휴대폰 번호입니다.");
        }
    }


    private String uploadProfileImage(final MultipartFile image) {
        String url = fileService.toUrls(image);
        fileService.fileUpload(image, url);
        return url;
    }
}
