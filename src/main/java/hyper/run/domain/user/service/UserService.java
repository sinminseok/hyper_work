package hyper.run.domain.user.service;

import hyper.run.domain.payment.dto.response.PaymentResponse;
import hyper.run.domain.user.dto.request.UserSignupRequest;
import hyper.run.domain.user.dto.response.UserProfileResponse;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void save(final UserSignupRequest userSignupRequest, final String encodePassword) {
        User user = userSignupRequest.toEntity(encodePassword);
        userRepository.save(user);
    }

    @Transactional
    public void editPassword(final String email, final String encodePassword) {
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), "존재하지 않는 이메일입니다.");
        user.updatePassword(encodePassword);
    }

    public boolean isExistEmail(final String email){
        Optional<User> byEmail = userRepository.findByEmail(email);
        return byEmail.isPresent();
    }

    @Transactional
    public void chargeCoupon(final String email, final int amount){
        //사용자가 실제로 결제했는지를 검증하는 메서드 만들면 좋을듯함
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), "존재하지 않는 사용자 이메일 입니다.");
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
        return "NONE";
    }

    /**
     * 내 프로필 조회 메서드
     */
    public UserProfileResponse getMyProfile(final String email) {
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), "존재하지 않는 사용자 이메일 입니다.");
        return UserProfileResponse.toProfileResponse(user);
    }

    /**
     * 내 잔여 쿠폰 조회 메서드
     */
    public int getMyCouponAmount(final String email){
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), "존재하지 않는 사용자 이메일 입니다.");
        return user.getCoupon();
    }

    /**
     * 자신의 모든 결제 내역을 조회하는 메서드
     */
    public List<PaymentResponse> getMyPayments(final String email){
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), "존재하지 않는 사용자 이메일 입니다.");
        return user.getPayments().stream()
                .map(p->PaymentResponse.toResponse(p))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(final String email){
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), "존재하지 않는 사용자 이메일 입니다.");
        userRepository.delete(user);
    }
}
