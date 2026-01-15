package hyper.run.domain.inquiry.service;

import hyper.run.domain.inquiry.entity.CustomerInquiry;
import hyper.run.domain.inquiry.entity.InquiryState;
import hyper.run.domain.inquiry.entity.InquiryType;
import hyper.run.domain.inquiry.repository.CustomerInquiryRepository;
import hyper.run.domain.payment.entity.Payment;
import hyper.run.domain.payment.entity.PaymentState;
import hyper.run.domain.payment.repository.PaymentRepository;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.dto.inquiry.AdminCustomerInquiryDetailResponse;
import hyper.run.dto.inquiry.AdminCustomerInquiryListResponse;
import hyper.run.utils.EmailService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerInquiryManagementService {

    private final CustomerInquiryRepository customerInquiryRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;

    public Page<AdminCustomerInquiryListResponse> getInquiryList(
            String filterState,
            String filterType,
            LocalDate startDate,
            LocalDate endDate,
            String sortBy,
            int page,
            int size
    ) {
        // 정렬 설정
        Sort sort = createSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Specification을 사용한 동적 쿼리 생성
        Specification<CustomerInquiry> spec = createSpecification(filterState, filterType, startDate, endDate);
        Page<CustomerInquiry> inquiries = customerInquiryRepository.findAll(spec, pageable);

        // User 정보 조회
        List<Long> userIds = inquiries.getContent().stream()
                .map(inquiry -> inquiry.getUser().getId())
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // DTO 변환
        List<AdminCustomerInquiryListResponse> content = inquiries.getContent().stream()
                .map(inquiry -> AdminCustomerInquiryListResponse.from(
                        inquiry,
                        userMap.get(inquiry.getUser().getId())
                ))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, inquiries.getTotalElements());
    }

    private Sort createSort(String sortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createDateTime");
        }

        return switch (sortBy) {
            case "createdAt" -> Sort.by(Sort.Direction.DESC, "createDateTime");
            case "state" -> Sort.by(Sort.Direction.ASC, "state");
            case "type" -> Sort.by(Sort.Direction.ASC, "type");
            case "userName" -> Sort.by(Sort.Direction.ASC, "user", "name");
            case "email" -> Sort.by(Sort.Direction.ASC, "email");
            case "phoneNumber" -> Sort.by(Sort.Direction.ASC, "user", "phoneNumber");
            default -> Sort.by(Sort.Direction.DESC, "createDateTime");
        };
    }

    private Specification<CustomerInquiry> createSpecification(
            String filterState,
            String filterType,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 날짜 범위 필터
            if (startDate != null && endDate != null) {
                LocalDateTime startDateTime = startDate.atStartOfDay();
                LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                predicates.add(criteriaBuilder.between(root.get("createDateTime"), startDateTime, endDateTime));
            }

            // 상태 필터
            if (filterState != null && !filterState.isEmpty() && !"ALL".equals(filterState)) {
                InquiryState state = InquiryState.valueOf(filterState);
                predicates.add(criteriaBuilder.equal(root.get("state"), state));
            }

            // 유형 필터
            if (filterType != null && !filterType.isEmpty() && !"ALL".equals(filterType)) {
                InquiryType type = InquiryType.valueOf(filterType);
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public AdminCustomerInquiryDetailResponse getInquiryDetail(Long inquiryId) {
        CustomerInquiry inquiry = customerInquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));

        User user = inquiry.getUser();

        return AdminCustomerInquiryDetailResponse.from(inquiry, user);
    }

    @Transactional
    public void sendAnswer(Long inquiryId, String answerContent) {
        CustomerInquiry inquiry = customerInquiryRepository.findById(inquiryId).orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));

        // 답변 저장
        inquiry.setAnswer(answerContent);
        inquiry.setState(InquiryState.SUCCESS);

        // 이메일 전송
        String subject = "[FunnyRun] 문의하신 내용에 대한 답변입니다.";
        String emailBody = String.format(
                "안녕하세요, %s님.\n\n" +
                        "문의하신 내용에 대한 답변입니다.\n\n" +
                        "===== 문의 내용 =====\n%s\n\n" +
                        "===== 답변 내용 =====\n%s\n\n" +
                        "추가 문의사항이 있으시면 언제든지 연락 주시기 바랍니다.\n\n" +
                        "감사합니다.\n" +
                        "FunnyRun 고객지원팀",
                inquiry.getUser().getName(),
                inquiry.getMessage(),
                answerContent
        );

        emailService.sendSimpleEmail(inquiry.getEmail(), subject, emailBody);
    }

    @Transactional
    public void deleteInquiry(Long inquiryId) {
        CustomerInquiry inquiry = customerInquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));

        customerInquiryRepository.delete(inquiry);
    }

    @Transactional
    public void approveRefund(Long inquiryId, String reason) {
        CustomerInquiry inquiry = customerInquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));

        if (inquiry.getType() != InquiryType.REFUND) {
            throw new IllegalStateException("환불 요청이 아닙니다.");
        }

        if (inquiry.getState() != InquiryState.WAITING) {
            throw new IllegalStateException("대기중인 환불 요청만 승인할 수 있습니다.");
        }

        // 환불 승인 처리 (쿠폰은 이미 차감된 상태이므로 추가 처리 불필요)
        inquiry.setState(InquiryState.SUCCESS);
        inquiry.setAnswer("환불 요청이 승인되었습니다. 승인 사유: " + reason);

        // Payment 상태 변경
        Payment payment = paymentRepository.findById(inquiry.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
        payment.setState(PaymentState.REFUND_COMPLETED);

        // 이메일 전송 (문의 시 입력한 이메일로 전송)
        String subject = "[FunnyRun] 환불 요청이 승인되었습니다.";
        String emailBody = String.format(
                "안녕하세요, %s님.\n\n" +
                        "요청하신 환불이 승인되었습니다.\n\n" +
                        "환불 금액: %,d원\n" +
                        "입금 계좌: %s %s\n" +
                        "승인 사유: %s\n\n" +
                        "영업일 기준 3-5일 이내에 환불 처리됩니다.\n\n" +
                        "감사합니다.\n" +
                        "FunnyRun 고객지원팀",
                inquiry.getUser().getName(),
                inquiry.getRefundPrice(),
                inquiry.getBankName(),
                inquiry.getAccountNumber(),
                reason
        );

        emailService.sendSimpleEmail(inquiry.getEmail(), subject, emailBody);
    }

    @Transactional
    public void rejectRefund(Long inquiryId, String reason) {
        CustomerInquiry inquiry = customerInquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));

        if (inquiry.getType() != InquiryType.REFUND) {
            throw new IllegalStateException("환불 요청이 아닙니다.");
        }

        if (inquiry.getState() != InquiryState.WAITING) {
            throw new IllegalStateException("대기중인 환불 요청만 거절할 수 있습니다.");
        }

        // Payment 조회
        Payment payment = paymentRepository.findById(inquiry.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        // User Lock 획득 후 쿠폰 복구 (환불 요청 시 차감된 쿠폰을 다시 증가)
        User user = userRepository.findByIdForUpdate(inquiry.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
        user.increaseCouponByAmount(payment.getCouponAmount());

        // 환불 거절 처리
        inquiry.setState(InquiryState.SUCCESS);
        inquiry.setAnswer("환불 요청이 거절되었습니다. 거절 사유: " + reason);

        // Payment 상태 변경
        payment.setState(PaymentState.REFUND_REJECTED);

        // 이메일 전송 (문의 시 입력한 이메일로 전송)
        String subject = "[FunnyRun] 환불 요청 처리 결과 안내";
        String emailBody = String.format(
                "안녕하세요, %s님.\n\n" +
                        "요청하신 환불이 거절되었습니다.\n\n" +
                        "거절 사유: %s\n\n" +
                        "차감되었던 쿠폰 %d개가 복구되었습니다.\n\n" +
                        "추가 문의사항이 있으시면 언제든지 연락 주시기 바랍니다.\n\n" +
                        "감사합니다.\n" +
                        "FunnyRun 고객지원팀",
                inquiry.getUser().getName(),
                reason,
                payment.getCouponAmount()
        );

        emailService.sendSimpleEmail(inquiry.getEmail(), subject, emailBody);
    }
}
