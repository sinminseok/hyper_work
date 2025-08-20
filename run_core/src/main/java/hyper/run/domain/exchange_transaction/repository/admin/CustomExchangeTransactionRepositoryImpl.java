package hyper.run.domain.exchange_transaction.repository.admin;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hyper.run.domain.exchange_transaction.dto.response.AdminExchangeTransactionResponse;
import hyper.run.domain.exchange_transaction.entity.ExchangeStatus;
import hyper.run.domain.exchange_transaction.entity.ExchangeTransaction;
import hyper.run.domain.inquiry.dto.request.InquirySearchRequest;
import hyper.run.domain.inquiry.dto.response.CustomerInquiryResponse;
import hyper.run.domain.inquiry.entity.CustomerInquiry;
import hyper.run.domain.inquiry.entity.InquiryState;
import hyper.run.domain.inquiry.entity.InquiryType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import hyper.run.domain.exchange_transaction.entity.QExchangeTransaction;
import hyper.run.domain.user.entity.QUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Repository
public class CustomExchangeTransactionRepositoryImpl implements CustomExchangeTransactionRepository {

    private final QExchangeTransaction exchangeTransaction = QExchangeTransaction.exchangeTransaction;
    private final QUser user = QUser.user;
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminExchangeTransactionResponse> findExchanges(LocalDate startDate, LocalDate endDate,String keyword, ExchangeStatus exchangeStatus, Pageable pageable) {

        BooleanBuilder whereClause = createWhereClause(startDate,endDate,keyword,exchangeStatus);

        List<AdminExchangeTransactionResponse> content = fetchContent(whereClause, pageable);

        Long total = fetchTotalCount(whereClause);

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 실제 데이터 목록(콘텐츠)을 조회
     */
    private List<AdminExchangeTransactionResponse> fetchContent(BooleanBuilder whereClause, Pageable pageable) {
        return queryFactory
                .select(Projections.constructor(AdminExchangeTransactionResponse.class,
                        exchangeTransaction.id,
                        exchangeTransaction.userId,
                        exchangeTransaction.amount,
                        exchangeTransaction.accountNumber,
                        exchangeTransaction.bankName,
                        exchangeTransaction.exchangeStatus,
                        user.name,
                        exchangeTransaction.createDateTime
                ))
                .from(exchangeTransaction)
                .where(whereClause)
                .join(user).on(exchangeTransaction.userId.eq(user.id))
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    /**
     * 전체 데이터 개수를 조회합니다.
     */
    private Long fetchTotalCount(BooleanBuilder whereClause) {
        Long count = queryFactory
                .select(exchangeTransaction.count())
                .from(exchangeTransaction)
                .where(whereClause)
                .fetchOne();
        return Objects.requireNonNullElse(count, 0L);
    }

    /**
     * WHERE 절에 들어갈 조건들을 생성하고 조합
     */
    private BooleanBuilder createWhereClause(LocalDate startDate, LocalDate endDate,String keyword, ExchangeStatus exchangeStatus) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(exchangeStatusEq(exchangeStatus))
                .and(dateRange(startDate,endDate))
                .and(keywordContains(keyword));
        return builder;
    }


    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> specifiers = new ArrayList<>();

        // PathBuilder를 통해 동적으로 User 엔티티의 필드 경로를 생성
        PathBuilder<ExchangeTransaction> pathBuilder = new PathBuilder<>(ExchangeTransaction.class, "exchangeTransaction");

        if (sort != null && !sort.isEmpty()) {
            for (Sort.Order order : sort) {
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                String property = order.getProperty();

                specifiers.add(new OrderSpecifier(direction, pathBuilder.get(property)));
            }
        }

        if(specifiers.isEmpty()){
        specifiers.add(new OrderSpecifier(Order.DESC, exchangeTransaction.createDateTime));
        }

        return specifiers.toArray(new OrderSpecifier[0]);
}
    private BooleanExpression dateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            // LocalDate를 LocalDateTime으로 변환하여 시간까지 포함한 범위 검색
            return exchangeTransaction.createDateTime.between(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        }
        return null;
    }

private BooleanExpression exchangeStatusEq(ExchangeStatus status) {
    if (status != null) {
        return exchangeTransaction.exchangeStatus.eq(status);
    }
    return null;
}

private BooleanExpression keywordContains(String keyword) {
    if (StringUtils.hasText(keyword)) {
        return exchangeTransaction.accountNumber.containsIgnoreCase(keyword)
                .or(exchangeTransaction.bankName.containsIgnoreCase(keyword))
                .or(user.name.containsIgnoreCase(keyword));
    }
    return null;
}
}
