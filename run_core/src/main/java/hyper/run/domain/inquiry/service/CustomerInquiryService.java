package hyper.run.domain.inquiry.service;

import hyper.run.domain.inquiry.dto.request.InquiryRequest;
import hyper.run.domain.inquiry.dto.request.InquirySearchRequest;
import hyper.run.domain.inquiry.dto.response.CustomerInquiryResponse;
import hyper.run.domain.inquiry.entity.CustomerInquiry;
import hyper.run.domain.inquiry.entity.InquiryState;
import hyper.run.domain.inquiry.entity.InquiryType;
import hyper.run.domain.inquiry.repository.CustomerInquiryRepository;
import hyper.run.domain.inquiry.repository.custom.CustomCustomerInquiryRepository;
import hyper.run.domain.payment.entity.Payment;
import hyper.run.domain.payment.entity.PaymentState;
import hyper.run.domain.payment.repository.PaymentRepository;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;

import static hyper.run.exception.ErrorMessages.NOT_EXIST_PAYMENT_ID;
import static hyper.run.exception.ErrorMessages.NOT_EXIST_USER_EMAIL;

@Service
@RequiredArgsConstructor
public class CustomerInquiryService {

    private final CustomerInquiryRepository repository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final CustomCustomerInquiryRepository customerInquiryRepository;

    @Transactional
    public void applyInquiry(final String email, final InquiryRequest request) {
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        if (request.getType() == InquiryType.REFUND && request.getPaymentId() != null) {
            saveRefundInquiry(user, request);
        } else {
            saveCommonInquiry(user, request);
        }
    }

    private void saveRefundInquiry(User user, InquiryRequest request) {
        Payment payment = OptionalUtil.getOrElseThrow(paymentRepository.findById(request.getPaymentId()), NOT_EXIST_PAYMENT_ID);
        payment.updateState(PaymentState.REFUND_REQUESTED);
        repository.save(request.toRefundInquiry(user));
    }

    private void saveCommonInquiry(User user, InquiryRequest request) {
        repository.save(request.toCommonInquiry(user));
    }

}
