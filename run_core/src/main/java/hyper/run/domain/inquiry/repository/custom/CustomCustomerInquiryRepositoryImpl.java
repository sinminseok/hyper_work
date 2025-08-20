package hyper.run.domain.inquiry.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hyper.run.domain.inquiry.dto.request.InquirySearchRequest;
import hyper.run.domain.inquiry.dto.response.CustomerInquiryResponse;
import hyper.run.domain.inquiry.entity.CustomerInquiry;
import hyper.run.domain.inquiry.entity.InquiryState;
import hyper.run.domain.inquiry.entity.InquiryType;
import hyper.run.domain.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import hyper.run.domain.user.entity.QUser;
import hyper.run.domain.inquiry.entity.QCustomerInquiry;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class CustomCustomerInquiryRepositoryImpl implements CustomCustomerInquiryRepository{

    private final JPAQueryFactory queryFactory;
    private final QCustomerInquiry inquiry = QCustomerInquiry.customerInquiry;
    private final QUser user = QUser.user;

    @Override
    public Page<CustomerInquiryResponse> searchInquiry(InquirySearchRequest inquiryRequest, Pageable pageable) {
        // 1. WHERE 절 조건을 한 번만 생성하여 재사용합니다.
        BooleanBuilder whereClause = createWhereClause(inquiryRequest);

        // 2. 조건에 맞는 데이터 목록(콘텐츠)을 조회합니다.
        List<CustomerInquiryResponse> content = fetchContent(whereClause, pageable);

        // 3. 조건에 맞는 전체 데이터 개수를 조회합니다.
        Long total = fetchTotalCount(whereClause);

        // 4. Page 객체로 조합하여 반환합니다.
        return new PageImpl<>(content, pageable, total);
    }

    /**
     *  실제 데이터 목록(콘텐츠)을 조회
     */
    private List<CustomerInquiryResponse> fetchContent(BooleanBuilder whereClause, Pageable pageable) {
        return queryFactory
                .select(Projections.constructor(CustomerInquiryResponse.class,
                        inquiry.id,
                        inquiry.inquiredAt,
                        inquiry.state,
                        inquiry.type,
                        user.name,
                        user.email,
                        user.phoneNumber,
                        inquiry.title,
                        inquiry.message,
                        inquiry.answer
                ))
                .from(inquiry)
                .leftJoin(inquiry.user, user)
                .where(whereClause)
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    /**
     *  전체 데이터 개수를 조회합니다.
     */
    private Long fetchTotalCount(BooleanBuilder whereClause) {
        Long count = queryFactory
                .select(inquiry.count())
                .from(inquiry)
                .leftJoin(inquiry.user, user)
                .where(whereClause)
                .fetchOne();
        return Objects.requireNonNullElse(count, 0L);
    }

    /**
     *  WHERE 절에 들어갈 조건들을 생성하고 조합
     */
    private BooleanBuilder createWhereClause(InquirySearchRequest request) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(dateRange(request.getStartDate(), request.getEndDate()))
                .and(inquiryStateEq(request.getState()))
                .and(inquiryTypeEq(request.getType()))
                .and(keywordContains(request.getKeyword()));
        return builder;
    }


    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> specifiers = new ArrayList<>();

        // PathBuilder를 통해 동적으로 User 엔티티의 필드 경로를 생성
        PathBuilder<CustomerInquiry> pathBuilder = new PathBuilder<>(CustomerInquiry.class, "customerInquiry");

        if (sort != null && !sort.isEmpty()) {
            for (Sort.Order order : sort) {
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                String property = order.getProperty();
                // 매핑 되어있는 다른 객체의 필드값을 쓸 때는 밑에 명시해서 정렬해야됨
                if ("name".equals(property)) {
                    specifiers.add(new OrderSpecifier(direction, inquiry.user.name));
                }else if("phoneNumber".equals(property)){
                    specifiers.add(new OrderSpecifier(direction,inquiry.user.phoneNumber));
                }
                else {
                    // 그 외 다른 속성들은 기존 방식대로 Payment 엔티티에서 찾음
                    specifiers.add(new OrderSpecifier(direction, pathBuilder.get(property)));
                }
            }
        }
        // 기본 정렬 (만약 아무 정렬 조건도 없다면)
        if (specifiers.isEmpty()) {
            specifiers.add(new OrderSpecifier(Order.DESC, inquiry.inquiredAt));
        }

        return specifiers.toArray(new OrderSpecifier[0]);
    }

    private BooleanExpression dateRange(LocalDate startDate,LocalDate endDate){
        if(startDate != null && endDate !=null){
            return inquiry.inquiredAt.between(startDate,endDate);
        }
        return null;
    }
    private BooleanExpression inquiryStateEq(InquiryState state){
        if(state != null){
            return inquiry.state.eq(state);
        }
        return null;
    }
    private BooleanExpression inquiryTypeEq(InquiryType type){
        if(type != null){
            return inquiry.type.eq(type);
        }
        return null;
    }
    private BooleanExpression keywordContains(String keyword){
        if (StringUtils.hasText(keyword)) {
            return inquiry.user.name.containsIgnoreCase(keyword)
                    .or(inquiry.user.email.containsIgnoreCase(keyword))
                    .or(inquiry.user.phoneNumber.containsIgnoreCase(keyword));
        }
        return null;
    }
}
