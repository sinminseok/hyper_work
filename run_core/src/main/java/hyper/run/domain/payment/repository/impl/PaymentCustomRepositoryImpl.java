package hyper.run.domain.payment.repository.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hyper.run.domain.payment.dto.request.PaymentSearchRequest;
import hyper.run.domain.payment.dto.response.AdminPaymentResponse;
import hyper.run.domain.payment.entity.Payment;
import hyper.run.domain.payment.entity.PaymentState;
import hyper.run.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import hyper.run.domain.user.entity.QUser;
import hyper.run.domain.payment.entity.QPayment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class PaymentCustomRepositoryImpl implements PaymentCustomRepository{

    private final JPAQueryFactory queryFactory;
    private final QPayment payment = QPayment.payment;
    private final QUser user = QUser.user;

    @Override
    public Page<AdminPaymentResponse> searchPayments(PaymentSearchRequest searchRequest, Pageable pageable) {
        // 1. WHERE 절 조건을 한 번만 생성하여 재사용합니다.
        BooleanBuilder whereClause = createWhereClause(searchRequest);

        // 2. 조건에 맞는 데이터 목록(콘텐츠)을 조회합니다.
        List<AdminPaymentResponse> content = fetchContent(whereClause, pageable);

        // 3. 조건에 맞는 전체 데이터 개수를 조회합니다.
        Long total = fetchTotalCount(whereClause);

        // 4. Page 객체로 조합하여 반환합니다.
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 실제 데이터 목록(콘텐츠)을 조회
     */
    private List<AdminPaymentResponse> fetchContent(BooleanBuilder whereClause, Pageable pageable) {
        return queryFactory
                .select(Projections.constructor(AdminPaymentResponse.class,
                        payment.id,
                        payment.paymentAt,
                        payment.paymentMethod,
                        payment.price,
                        user.name,
                        payment.state
                ))
                .from(payment)
                .leftJoin(payment.user, user)
                .where(whereClause)
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    /**
     * 전체 데이터 개수를 조회
     */
    private Long fetchTotalCount(BooleanBuilder whereClause) {
        Long count = queryFactory
                .select(payment.count())
                .from(payment)
                .leftJoin(payment.user, user)
                .where(whereClause)
                .fetchOne();
        return Objects.requireNonNullElse(count, 0L);
    }

    /**
     *  WHERE 절에 들어갈 조건들을 생성하고 조합
     */
    private BooleanBuilder createWhereClause(PaymentSearchRequest request) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(dateRange(request.getStartDate(), request.getEndDate()))
                .and(priceRange(request.getMinAmount(), request.getMaxAmount()))
                .and(paymentStateEq(request.getState()))
                .and(keywordContains(request.getKeyword()));
        return builder;
    }


    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> specifiers = new ArrayList<>();

        // PathBuilder를 통해 동적으로 User 엔티티의 필드 경로를 생성
        PathBuilder<Payment> pathBuilder = new PathBuilder<>(Payment.class, "payment");

        if (sort != null && !sort.isEmpty()) {
            for (Sort.Order order : sort) {
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                String property = order.getProperty();
                if ("name".equals(property)) {
                    specifiers.add(new OrderSpecifier(direction, payment.user.name));
                } else {
                    // 그 외 다른 속성들은 기존 방식대로 Payment 엔티티에서 찾음
                    specifiers.add(new OrderSpecifier(direction, pathBuilder.get(property)));
                }
            }
        }

        // 기본 정렬 (만약 아무 정렬 조건도 없다면)
        if (specifiers.isEmpty()) {
            specifiers.add(new OrderSpecifier(Order.DESC, payment.paymentAt));
        }

        return specifiers.toArray(new OrderSpecifier[0]);
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
        if(paymentState != null){
            return payment.state.eq(paymentState);
        }
        return null;
    }

    private BooleanExpression keywordContains(String keyword) {
        if (StringUtils.hasText(keyword)) {
            return payment.user.name.containsIgnoreCase(keyword);
        }
        return null;
    }
}
