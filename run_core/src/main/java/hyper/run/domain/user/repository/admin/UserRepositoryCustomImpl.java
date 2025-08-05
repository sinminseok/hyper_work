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

import java.util.List;

import static hyper.run.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    // 카테고리 별 커스텀 조회
    @Override
    public Page<User> searchUsers(String searchCategory, String keyword, Pageable pageable) {
        BooleanBuilder builder = createWhereClause(searchCategory, keyword);

        List<User> content = queryFactory
                .selectFrom(user)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable.getSort())) // ✨ 동적 정렬 적용
                .fetch();

        long total = queryFactory
                .selectFrom(user)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
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
            case "birth": builder.and(user.brith.eq(keyword)); break;
        }
        return builder;
    }

    /**
     * 조건별(카테고리) 정렬
     */
    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        if (sort.isUnsorted()) {
            return new OrderSpecifier[]{new OrderSpecifier<>(Order.DESC, user.id)}; // 기본 정렬: ID 내림차순
        }

        // PathBuilder를 사용해 어떤 필드(property)로든 정렬할 수 있게 함
        PathBuilder<User> pathBuilder = new PathBuilder<>(User.class, "user");

        return sort.stream()
                .map(order -> {
                    Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                    return new OrderSpecifier<>(direction, pathBuilder.get(order.getProperty(),Comparable.class));
                })
                .toArray(OrderSpecifier[]::new);
    }
    }


