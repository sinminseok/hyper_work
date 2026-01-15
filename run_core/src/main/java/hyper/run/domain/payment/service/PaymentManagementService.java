package hyper.run.domain.payment.service;

import hyper.run.domain.inquiry.entity.CustomerInquiry;
import hyper.run.domain.inquiry.entity.InquiryState;
import hyper.run.domain.inquiry.entity.InquiryType;
import hyper.run.domain.inquiry.repository.CustomerInquiryRepository;
import hyper.run.domain.payment.entity.Payment;
import hyper.run.domain.payment.entity.PaymentState;
import hyper.run.domain.payment.event.RefundApprovedEvent;
import hyper.run.domain.payment.event.RefundRejectedEvent;
import hyper.run.domain.payment.repository.PaymentRepository;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.dto.payment.AdminPaymentDetailResponse;
import hyper.run.dto.payment.AdminPaymentListResponse;
import hyper.run.utils.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentManagementService {

    private final PaymentRepository paymentRepository;
    private final CustomerInquiryRepository customerInquiryRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ApplicationEventPublisher eventPublisher;

    public Page<AdminPaymentListResponse> getPaymentList(
            String filterStatus,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDateTime"));

        // Specification을 사용한 동적 쿼리 생성
        Specification<Payment> spec = createSpecification(filterStatus, startDate, endDate);
        Page<Payment> payments = paymentRepository.findAll(spec, pageable);

        // 환불 요청이 있는 결제 ID 목록 조회 (type=REFUND, state=WAITING)
        List<Long> paymentIds = payments.getContent().stream()
                .map(Payment::getId)
                .collect(Collectors.toList());

        List<CustomerInquiry> refundInquiries = customerInquiryRepository
                .findByPaymentIdInAndTypeAndState(paymentIds, InquiryType.REFUND, InquiryState.WAITING);

        // paymentId를 키로 하는 Map 생성
        Map<Long, CustomerInquiry> refundInquiryMap = refundInquiries.stream()
                .collect(Collectors.toMap(CustomerInquiry::getPaymentId, inquiry -> inquiry));

        // 필터가 "REFUND_REQUESTED"인 경우, 환불 요청이 있는 것만 필터링
        List<AdminPaymentListResponse> content = payments.getContent().stream()
                .filter(payment -> {
                    if ("REFUND_REQUESTED".equals(filterStatus)) {
                        return refundInquiryMap.containsKey(payment.getId());
                    }
                    return true;
                })
                .map(payment -> AdminPaymentListResponse.from(
                        payment,
                        refundInquiryMap.get(payment.getId())
                ))
                .collect(Collectors.toList());

        // REFUND_REQUESTED 필터링 시 total count 재계산
        long totalElements = "REFUND_REQUESTED".equals(filterStatus)
                ? content.size()
                : payments.getTotalElements();

        return new PageImpl<>(content, pageable, totalElements);
    }

    private Specification<Payment> createSpecification(String filterStatus, LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 날짜 범위 필터
            if (startDate != null && endDate != null) {
                LocalDateTime startDateTime = startDate.atStartOfDay();
                LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                predicates.add(criteriaBuilder.between(root.get("createDateTime"), startDateTime, endDateTime));
            }

            // 상태 필터
            if (filterStatus != null && !filterStatus.isEmpty() && !"ALL".equals(filterStatus)) {
                if ("REFUND_REQUESTED".equals(filterStatus)) {
                    // REFUND_REQUESTED는 Payment.state로 직접 필터링하지 않음 (CustomerInquiry에서 처리)
                    // 여기서는 환불 가능한 상태들만 조회 (PAYMENT_COMPLETED 또는 실제 환불 요청 상태)
                    predicates.add(criteriaBuilder.or(
                            criteriaBuilder.equal(root.get("state"), PaymentState.PAYMENT_COMPLETED),
                            criteriaBuilder.equal(root.get("state"), PaymentState.REFUND_REQUESTED)
                    ));
                } else {
                    // 다른 상태는 Payment.state로 직접 필터링
                    PaymentState state = PaymentState.valueOf(filterStatus);
                    predicates.add(criteriaBuilder.equal(root.get("state"), state));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public AdminPaymentDetailResponse getPaymentDetail(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        // 환불 요청 조회 (type=REFUND, state=WAITING)
        CustomerInquiry refundInquiry = customerInquiryRepository
                .findByPaymentIdAndTypeAndState(paymentId, InquiryType.REFUND, InquiryState.WAITING)
                .orElse(null);

        return AdminPaymentDetailResponse.from(payment, refundInquiry);
    }

    @Transactional
    public void approveRefund(Long paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        // 환불 요청 조회 (type=REFUND, state=WAITING)
        CustomerInquiry inquiry = customerInquiryRepository.findByPaymentIdAndTypeAndState(paymentId, InquiryType.REFUND, InquiryState.WAITING).orElseThrow(() -> new IllegalArgumentException("환불 요청 정보를 찾을 수 없습니다."));

        // 환불 승인 이벤트 발행
        RefundApprovedEvent event = new RefundApprovedEvent(
                payment.getId(),
                inquiry.getId(),
                inquiry.getUser().getId(),
                reason,
                inquiry.getUser().getName(),
                inquiry.getEmail(),
                inquiry.getRefundPrice(),
                inquiry.getBankName(),
                inquiry.getAccountNumber()
        );

        eventPublisher.publishEvent(event);
    }

    @Transactional
    public void rejectRefund(Long paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        // 환불 요청 조회 (type=REFUND, state=WAITING)
        CustomerInquiry inquiry = customerInquiryRepository
                .findByPaymentIdAndTypeAndState(paymentId, InquiryType.REFUND, InquiryState.WAITING)
                .orElseThrow(() -> new IllegalArgumentException("환불 요청 정보를 찾을 수 없습니다."));

        // 환불 거절 이벤트 발행
        RefundRejectedEvent event = new RefundRejectedEvent(
                payment.getId(),
                inquiry.getId(),
                payment.getUser().getId(),
                reason,
                payment.getCouponAmount(),
                inquiry.getUser().getName(),
                inquiry.getEmail()
        );

        eventPublisher.publishEvent(event);
    }
}
