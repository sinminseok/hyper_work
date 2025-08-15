package hyper.run.domain.game.repository.admin;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import hyper.run.domain.game.entity.QGame;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class GameRepositoryCustomImpl implements GameRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    private final QGame game = QGame.game;

    @Override
    public Page<Game> findGamesByCriteria(LocalDateTime createdAfter, LocalDateTime createdBefore, GameStatus status, String keyword, Pageable pageable) {

        List<Game> content = queryFactory
                .selectFrom(game)
                .where(
                        // 동적 쿼리 조건들을 조합합니다.
                        createdAtGoe(createdAfter),
                        createdAtLt(createdBefore),
                        statusEq(status),
                        nameContains(keyword)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(game.count())
                .from(game)
                .where(
                        createdAtGoe(createdAfter),
                        createdAtLt(createdBefore),
                        statusEq(status),
                        nameContains(keyword)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
    private BooleanExpression createdAtGoe(LocalDateTime createdAfter) {
        return createdAfter != null ? game.createdAt.goe(createdAfter) : null;
    }

    private BooleanExpression createdAtLt(LocalDateTime createdBefore) {
        return createdBefore != null ? game.createdAt.lt(createdBefore) : null;
    }

    private BooleanExpression statusEq(GameStatus status) {
        return status != null ? game.status.eq(status) : null;
    }

    private BooleanExpression nameContains(String keyword) {
        return StringUtils.hasText(keyword) ? game.name.containsIgnoreCase(keyword) : null;
    }
}

