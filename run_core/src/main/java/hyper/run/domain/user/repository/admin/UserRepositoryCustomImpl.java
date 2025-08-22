package hyper.run.domain.user.repository.admin;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hyper.run.domain.user.entity.QUser;
import hyper.run.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Repository
@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QUser user = QUser.user;

    // 카테고리 별 커스텀 조회
    @Override
    public Page<User> searchUsers(String searchCategory, String keyword, Pageable pageable) {
        // 1. WHERE 절 조건을 생성합니다.
        BooleanBuilder whereClause = createWhereClause(searchCategory, keyword);

        // 2. 조건에 맞는 데이터 목록(콘텐츠)을 조회합니다.
        List<User> content = fetchContent(whereClause, pageable);

        // 3. 조건에 맞는 전체 데이터 개수를 조회합니다.
        Long total = fetchTotalCount(whereClause);

        // 4. Page 객체로 조합하여 반환합니다.
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 실제 데이터 목록(콘텐츠)을 조회합니다.
     */
    private List<User> fetchContent(BooleanBuilder whereClause, Pageable pageable) {
        return queryFactory
                .selectFrom(user)
                .where(whereClause)
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    /**
     * 전체 데이터 개수를 조회합니다.
     *
     */
    private Long fetchTotalCount(BooleanBuilder whereClause) {
        Long count = queryFactory
                .select(user.count())
                .from(user)
                .where(whereClause)
                .fetchOne();
        return Objects.requireNonNullElse(count, 0L);
    }

    /**
     * 조건별 커스텀 조회 메서드
     */
    private BooleanBuilder createWhereClause(String searchCategory, String keyword) {
        BooleanBuilder builder = new BooleanBuilder();
        if (!StringUtils.hasText(searchCategory) || !StringUtils.hasText(keyword)) {
            return builder;
        }
        switch (searchCategory.toLowerCase()) {
            case "email": builder.and(user.email.containsIgnoreCase(keyword)); break;
            case "name": builder.and(user.name.containsIgnoreCase(keyword)); break;
            case "phoneNumber": builder.and(user.phoneNumber.contains(keyword)); break;
            case "birth": builder.and(user.birth.eq(keyword)); break;
        }
        return builder;
    }

    /**
     * 조건별(카테고리) 정렬
     */
    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> specifiers = new ArrayList<>();

        // PathBuilder를 통해 동적으로 User 엔티티의 필드 경로를 생성
        PathBuilder<User> pathBuilder = new PathBuilder<>(User.class, "user");

        if (sort != null && !sort.isEmpty()) {
            for (Sort.Order order : sort) {
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                String property = order.getProperty();
                specifiers.add(new OrderSpecifier(direction, pathBuilder.get(property)));
            }
        }

        // 기본 정렬 (만약 아무 정렬 조건도 없다면)
        if (specifiers.isEmpty()) {
            specifiers.add(new OrderSpecifier(Order.DESC, user.id)); //  최신 유저 순
        }

        return specifiers.toArray(new OrderSpecifier[0]);
    }
    }


