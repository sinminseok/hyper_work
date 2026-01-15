package hyper.run.domain.exchange_transaction.service;

import hyper.run.domain.exchange_transaction.entity.ExchangeStatus;
import hyper.run.domain.exchange_transaction.entity.ExchangeTransaction;
import hyper.run.domain.exchange_transaction.repository.ExchangeTransactionRepository;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.dto.exchange.AdminExchangeTransactionDetailResponse;
import hyper.run.dto.exchange.AdminExchangeTransactionListResponse;
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
public class ExchangeTransactionManagementService {

    private final ExchangeTransactionRepository exchangeTransactionRepository;
    private final UserRepository userRepository;

    public Page<AdminExchangeTransactionListResponse> getExchangeTransactionList(
            String filterStatus,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDateTime"));

        // Specification을 사용한 동적 쿼리 생성
        Specification<ExchangeTransaction> spec = createSpecification(filterStatus, startDate, endDate);
        Page<ExchangeTransaction> transactions = exchangeTransactionRepository.findAll(spec, pageable);

        // User 정보 조회
        List<Long> userIds = transactions.getContent().stream()
                .map(ExchangeTransaction::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // DTO 변환
        List<AdminExchangeTransactionListResponse> content = transactions.getContent().stream()
                .map(transaction -> AdminExchangeTransactionListResponse.from(
                        transaction,
                        userMap.get(transaction.getUserId())
                ))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, transactions.getTotalElements());
    }

    private Specification<ExchangeTransaction> createSpecification(String filterStatus, LocalDate startDate, LocalDate endDate) {
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
                ExchangeStatus status = ExchangeStatus.valueOf(filterStatus);
                predicates.add(criteriaBuilder.equal(root.get("exchangeStatus"), status));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public AdminExchangeTransactionDetailResponse getExchangeTransactionDetail(Long transactionId) {
        ExchangeTransaction transaction = exchangeTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("환전 정보를 찾을 수 없습니다."));

        User user = userRepository.findById(transaction.getUserId()).orElse(null);

        return AdminExchangeTransactionDetailResponse.from(transaction, user);
    }

    @Transactional
    public void approveExchange(Long transactionId) {
        ExchangeTransaction transaction = exchangeTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("환전 정보를 찾을 수 없습니다."));

        if (transaction.getExchangeStatus() != ExchangeStatus.REQUESTED) {
            throw new IllegalStateException("신청된 환전만 승인할 수 있습니다.");
        }

        // ExchangeStatus를 COMPLETED로 변경
        transaction.setExchangeStatus(ExchangeStatus.COMPLETED);
    }

    @Transactional
    public void rejectExchange(Long transactionId) {
        ExchangeTransaction transaction = exchangeTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("환전 정보를 찾을 수 없습니다."));

        if (transaction.getExchangeStatus() != ExchangeStatus.REQUESTED) {
            throw new IllegalStateException("신청된 환전만 거절할 수 있습니다.");
        }

        // ExchangeStatus를 CANCELLED로 변경
        transaction.setExchangeStatus(ExchangeStatus.CANCELLED);

        // 사용자 포인트 복구
        User user = userRepository.findById(transaction.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.increasePoint(transaction.getAmount());
    }
}
