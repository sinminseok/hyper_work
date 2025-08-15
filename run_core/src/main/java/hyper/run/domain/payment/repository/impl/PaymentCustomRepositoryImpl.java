package hyper.run.domain.payment.repository.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hyper.run.domain.payment.dto.request.PaymentSearchRequest;
import hyper.run.domain.payment.dto.response.AdminPaymentResponse;
import hyper.run.domain.payment.entity.PaymentState;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import hyper.run.domain.user.entity.QUser;
import hyper.run.domain.payment.entity.QPayment;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentCustomRepositoryImpl implements PaymentCustomRepository{

    private final JPAQueryFactory queryFactory;
    private final QPayment payment = QPayment.payment;
    private final QUser user = QUser.user;

    @Override
    public Page<AdminPaymentResponse> searchPayments(PaymentSearchRequest searchRequest, Pageable pageable) {

        List<AdminPaymentResponse> content = queryFactory
                .select(Projections.constructor(AdminPaymentResponse.class,
                        payment.id,
                        payment.paymentAt,
                        payment.paymentMethod,
                        payment.price,
                        user.name,
                        payment.state // DB에서는 paymentState
                ))
                .from(payment)
                .leftJoin(payment.user, user)   // Payment와 연관된 User 조인
                .where(
                        dateRange(searchRequest.getStartDate(),searchRequest.getEndDate()),
                        priceRange(searchRequest.getMinPrice(), searchRequest.getMaxPrice()),
                        paymentStateEq(searchRequest.getState()),
                        keywordContains(searchRequest.getKeyword())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(payment.paymentAt.desc()) // 최신순 정렬 예시
                .fetch();

        // 전체 카운트 조회 쿼리 (페이지네이션을 위해 필요)
        Long total = queryFactory
                .select(payment.count())
                .from(payment)
                .leftJoin(payment.user, user)
                .where(
                        dateRange(searchRequest.getStartDate(),searchRequest.getEndDate()),
                        priceRange(searchRequest.getMinPrice(), searchRequest.getMaxPrice()),
                        paymentStateEq(searchRequest.getState()),
                        keywordContains(searchRequest.getKeyword())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression dateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            // LocalDate를 LocalDateTime으로 변환하여 시간까지 포함한 범위 검색
            return payment.paymentAt.between(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        }
        return null;
    }

    private BooleanExpression priceRange(Integer minPrice, Integer maxPrice) {
        if (minPrice != null && maxPrice != null) {
            return payment.price.between(minPrice, maxPrice);
        }
        return null;
    }

    private BooleanExpression paymentStateEq(PaymentState paymentState) {
        return paymentState != null ? payment.state.eq(paymentState) : null;
    }

    private BooleanExpression keywordContains(String keyword) {
        if (StringUtils.hasText(keyword)) {
            return user.name.containsIgnoreCase(keyword);
        }
        return null;
    }
}
