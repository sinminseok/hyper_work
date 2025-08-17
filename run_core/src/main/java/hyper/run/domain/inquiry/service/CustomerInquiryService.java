package hyper.run.domain.inquiry.service;

import hyper.run.domain.inquiry.dto.request.InquiryRequest;
import hyper.run.domain.inquiry.entity.InquiryType;
import hyper.run.domain.inquiry.repository.CustomerInquiryRepository;
import hyper.run.domain.payment.entity.Payment;
import hyper.run.domain.payment.entity.PaymentState;
import hyper.run.domain.payment.repository.PaymentRepository;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static hyper.run.exception.ErrorMessages.NOT_EXIST_PAYMENT_ID;
import static hyper.run.exception.ErrorMessages.NOT_EXIST_USER_EMAIL;

@Service
@RequiredArgsConstructor
public class CustomerInquiryService {

    private final CustomerInquiryRepository repository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public void applyInquiry(final String email, final InquiryRequest request) {
        Long userId = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL).getId();
        if (request.getType() == InquiryType.REFUND && request.getPaymentId() != null) {
            saveRefundInquiry(userId, request);
        } else {
            saveCommonInquiry(userId, request);
        }
    }

    private void saveRefundInquiry(Long userId, InquiryRequest request) {
        Payment payment = OptionalUtil.getOrElseThrow(paymentRepository.findById(request.getPaymentId()), NOT_EXIST_PAYMENT_ID);
        payment.updateState(PaymentState.REFUND_REQUESTED);
        repository.save(request.toRefundInquiry(userId));
    }

    private void saveCommonInquiry(Long userId, InquiryRequest request) {
        repository.save(request.toCommonInquiry(userId));
    }

    public
}
