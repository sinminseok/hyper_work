package hyper.run.domain.game.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hyper.run.domain.game.entity.*;
import hyper.run.domain.game.repository.GameRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static hyper.run.domain.game.entity.QGame.game;

@RequiredArgsConstructor
@Repository
public class GameRepositoryCustomImpl implements GameRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QGame game = QGame.game;


    @Override
    public List<Game> findGamesOrderByPrizeWithoutCursor(LocalDateTime now, int limit) {
        return queryFactory
                .selectFrom(game)
                .where(game.startAt.gt(now))
                .orderBy(game.totalPrize.desc(), game.id.asc())
                .offset(3)
                .limit(limit)
                .fetch();
    }

    @Override
    public Optional<Game> findByGameConditions(LocalDateTime startAt, GameDistance distance, GameType type, ActivityType activityType) {
        Game result = queryFactory
                .selectFrom(game)
                .where(
                        game.startAt.eq(startAt),
                        game.distance.eq(distance),
                        game.type.eq(type),
                        game.activityType.eq(activityType)
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<Game> findGamesOrderByPrizeWithCursor(LocalDateTime now, double cursorTotalPrize, Long cursorGameId, int limit) {
        return queryFactory
                .selectFrom(game)
                .where(
                        game.startAt.gt(now),
                        cursorCondition(cursorTotalPrize, cursorGameId)
                )
                .orderBy(game.totalPrize.desc(), game.id.asc())
                .limit(limit)
                .fetch();
    }

    private BooleanExpression cursorCondition(double cursorTotalPrize, Long cursorGameId) {
        BooleanExpression prizeLessThan = game.totalPrize.lt(cursorTotalPrize);
        BooleanExpression prizeEqualsAndIdGreaterThan = game.totalPrize.eq(cursorTotalPrize)
                .and(game.id.gt(cursorGameId));

        return prizeLessThan.or(prizeEqualsAndIdGreaterThan);
    }

    @Override
    public Page<Game> findGamesByCriteria(LocalDateTime createdAfter, LocalDateTime createdBefore, AdminGameStatus status, String keyword, Pageable pageable) {

        BooleanBuilder whereClause = createWhereConditions(createdAfter, createdBefore, status, keyword);

        List<Game> content = fetchContent(whereClause, pageable);

        Long total = fetchTotalCount(whereClause);

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 실제 데이터 목록(콘텐츠)을 조회
     */
    private List<Game> fetchContent(BooleanBuilder whereClause, Pageable pageable) {
        return queryFactory.selectFrom(game).where(whereClause).orderBy(getOrderSpecifiers(pageable.getSort())).offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();
    }

    /**
     * 전체 데이터 개수를 조회
     */
    private Long fetchTotalCount(BooleanBuilder whereClause) {
        Long count = queryFactory.select(game.count()).from(game).where(whereClause).fetchOne();
        return Objects.requireNonNullElse(count, 0L);
    }

    /**
     * WHERE 절에 들어갈 조건들을 생성하고 조합합니다.
     */
    private BooleanBuilder createWhereConditions(LocalDateTime createdAfter, LocalDateTime createdBefore, AdminGameStatus adminGameStatus, String keyword) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(createdAtGoe(createdAfter));
        builder.and(createdAtLt(createdBefore));
        builder.and(statusEq(adminGameStatus));
        builder.and(nameContains(keyword));

        return builder;
    }


    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        if (sort != null && !sort.isEmpty()) {
            PathBuilder<Game> pathBuilder = new PathBuilder<>(Game.class, "game");
            for (Sort.Order order : sort) {
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                String property = order.getProperty();
                orderSpecifiers.add(new OrderSpecifier(direction, pathBuilder.get(property)));
            }
        }

        // 기본 정렬: 만약 정렬 조건이 없다면 최신순으로 정렬
        if (orderSpecifiers.isEmpty()) {
            orderSpecifiers.add(new OrderSpecifier(Order.DESC, game.createDateTime));
        }

        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }

    private BooleanExpression createdAtGoe(LocalDateTime createdAfter) {
        if (createdAfter != null) {
            return game.createDateTime.goe(createdAfter);
        }
        return null;
    }

    private BooleanExpression createdAtLt(LocalDateTime createdBefore) {
        if (createdBefore != null) {
            return game.createDateTime.lt(createdBefore.plusDays(1));
        }
        return null;
    }

    private BooleanExpression statusEq(AdminGameStatus status) {
        if (status != null) {
            return game.adminGameStatus.eq(status);
        }
        return null;
    }

    private BooleanExpression nameContains(String keyword) {
        if (StringUtils.hasText(keyword)) {
            return game.name.containsIgnoreCase(keyword);
        }
        return null;
    }

    @Override
    public List<Game> findGamesByYearAndMonth(int year, int month, List<Long> gameIds) {
        LocalDateTime monthStart = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);
        LocalDateTime now = LocalDateTime.now();

        return queryFactory
                .selectFrom(game)
                .where(
                        game.id.in(gameIds),
                        game.endAt.between(monthStart, monthEnd),
                        game.endAt.lt(now)
                )
                .orderBy(game.endAt.asc())
                .fetch();
    }
}




