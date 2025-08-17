package hyper.run.domain.inquiry.repository.custom;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hyper.run.domain.inquiry.dto.request.InquirySearchRequest;
import hyper.run.domain.inquiry.dto.response.CustomerInquiryResponse;
import hyper.run.domain.inquiry.entity.CustomerInquiry;
import hyper.run.domain.inquiry.entity.InquiryState;
import hyper.run.domain.inquiry.entity.InquiryType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import hyper.run.domain.user.entity.QUser;
import hyper.run.domain.inquiry.entity.QCustomerInquiry;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomCustomerInquiryRepositoryImpl implements CustomCustomerInquiryRepository{

    private final JPAQueryFactory queryFactory;
    private final QCustomerInquiry inquiry = QCustomerInquiry.customerInquiry;
    private final QUser user = QUser.user;

    @Override
    public Page<CustomerInquiryResponse> searchInquiry(InquirySearchRequest inquiryRequest, Pageable pageable) {
        List<CustomerInquiryResponse> content = queryFactory
                .select(Projections.constructor(CustomerInquiryResponse.class,
                        inquiry.inquired_at,
                        inquiry.state,
                        inquiry.type,
                        user.name,
                        user.email,
                        user.phoneNumber,
                        inquiry.title,
                        inquiry.message
                        ))
                .from(inquiry)
                .leftJoin(inquiry.user,user)
                .where(
                        dateRange(inquiryRequest.getStartDate(),inquiryRequest.getEndDate()),
                        inquiryStateEq(inquiryRequest.getState()),
                        inquiryTypeEq(inquiryRequest.getType()),
                        keywordContains(inquiryRequest.getKeyword())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(inquiry.inquiredAt.desc())
                .fetch();

        Long total = queryFactory
                .select(inquiry.count())
                .from(inquiry)
                .leftJoin(inquiry.user,user)
                .where(
                        dateRange(inquiryRequest.getStartDate(),inquiryRequest.getEndDate()),
                        inquiryStateEq(inquiryRequest.getState()),
                        inquiryTypeEq(inquiryRequest.getType()),
                        keywordContains(inquiryRequest.getKeyword())
                )
                .fetchOne();
        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression dateRange(LocalDate startDate,LocalDate endDate){
        if(startDate != null && endDate !=null){
            return inquiry.inquiredAt.between(startDate,endDate);
        }
        return null;
    }
    private BooleanExpression inquiryStateEq(InquiryState state){
        return state != null ? inquiry.state.eq(state) : null;
    }
    private BooleanExpression inquiryTypeEq(InquiryType type){
        return type != null ? inquiry.type.eq(type) : null;
    }
    private BooleanExpression keywordContains(String keyword){
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return user.name.containsIgnoreCase(keyword)
                .or(user.email.containsIgnoreCase(keyword))
                .or(user.phoneNumber.containsIgnoreCase(keyword));
    }
}
