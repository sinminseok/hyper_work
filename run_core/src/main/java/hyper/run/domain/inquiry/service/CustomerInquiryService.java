package hyper.run.domain.inquiry.service;

import hyper.run.domain.inquiry.dto.request.InquiryRequest;
import hyper.run.domain.inquiry.entity.InquiryType;
import hyper.run.domain.inquiry.event.CommonInquiryCreatedEvent;
import hyper.run.domain.inquiry.event.RefundInquiryCreatedEvent;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static hyper.run.exception.ErrorMessages.NOT_EXIST_USER_EMAIL;

/**
 * 문의 서비스
 * - 역할: 이벤트 발행 (조율자)
 * - 실제 비즈니스 로직은 InquiryListener에서 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerInquiryService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 환불 신청과 일반 문의 사항을 같은 API에서 처리
     * - 환불: RefundInquiryCreatedEvent 발행
     * - 일반: CommonInquiryCreatedEvent 발행
     */
    @Transactional
    public void applyInquiry(final String email, final InquiryRequest request) {
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        if (request.getType() == InquiryType.REFUND && request.getPaymentId() != null) {
            publishRefundInquiryEvent(user, request);
        } else {
            publishCommonInquiryEvent(user, request);
        }
    }


    private void publishRefundInquiryEvent(User user, InquiryRequest request) {

        RefundInquiryCreatedEvent event = RefundInquiryCreatedEvent.of(
                user.getId(),
                request.getEmail(),
                request.getPaymentId(),
                request.getRefundPrice(),
                request.getRefundType(),
                request.getAccountNumber(),
                request.getBankName(),
                request.getTitle(),
                request.getMessage()
        );

        eventPublisher.publishEvent(event);
    }


    private void publishCommonInquiryEvent(User user, InquiryRequest request) {

        CommonInquiryCreatedEvent event = CommonInquiryCreatedEvent.of(
                user.getId(),
                request.getEmail(),
                request.getType(),
                request.getTitle(),
                request.getMessage()
        );

        eventPublisher.publishEvent(event);
    }
}
