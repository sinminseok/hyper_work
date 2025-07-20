package hyper.run.domain.payment.service;

import hyper.run.domain.payment.dto.request.PaymentRequest;
import hyper.run.domain.payment.dto.response.PaymentResponse;
import hyper.run.domain.payment.entity.Payment;
import hyper.run.domain.payment.entity.PaymentState;
import hyper.run.domain.payment.repository.PaymentRepository;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repository;
    private final UserRepository userRepository;

    /**
     * 결제 메서드
     */
    public void pay(final String email, final PaymentRequest request){
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), "존재하지 않는 이메일입니다.");
        Payment payment = request.toEntity(user);
        user.addPayment(payment);
        repository.save(payment); // Payment 저장
    }

    /**
     * 환불 가능한 결제 내역 모두 조회 메서드
     */
    public List<PaymentResponse> findPossibleRefundPayment(final String email) {
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), "존재하지 않는 이메일입니다.");
        return user.getPayments().stream()
                .filter(payment -> payment.getState() == PaymentState.PAYMENT_COMPLETED) //결제 완료된 내역들만
                .map(PaymentResponse::toResponse)
                .collect(Collectors.toList());
    }
}
