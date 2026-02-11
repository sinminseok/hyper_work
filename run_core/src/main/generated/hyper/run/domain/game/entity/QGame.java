package hyper.run.domain.game.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QGame is a Querydsl query type for Game
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGame extends EntityPathBase<Game> {

    private static final long serialVersionUID = -2040164282L;

    public static final QGame game = new QGame("game");

    public final hyper.run.domain.common.QBaseTimeEntity _super = new hyper.run.domain.common.QBaseTimeEntity(this);

    public final EnumPath<ActivityType> activityType = createEnum("activityType", ActivityType.class);

    public final EnumPath<AdminGameStatus> adminGameStatus = createEnum("adminGameStatus", AdminGameStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDateTime = _super.createDateTime;

    public final EnumPath<GameDistance> distance = createEnum("distance", GameDistance.class);

    public final DateTimePath<java.time.LocalDateTime> endAt = createDateTime("endAt", java.time.LocalDateTime.class);

    public final NumberPath<Double> firstPlacePrize = createNumber("firstPlacePrize", Double.class);

    public final StringPath firstUserName = createString("firstUserName");

    public final NumberPath<Double> fourthPlacePrize = createNumber("fourthPlacePrize", Double.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDateTime = _super.modifiedDateTime;

    public final StringPath name = createString("name");

    public final NumberPath<Double> otherPlacePrize = createNumber("otherPlacePrize", Double.class);

    public final NumberPath<Integer> participatedCount = createNumber("participatedCount", Integer.class);

    public final NumberPath<Double> secondPlacePrize = createNumber("secondPlacePrize", Double.class);

    public final StringPath secondUserName = createString("secondUserName");

    public final DateTimePath<java.time.LocalDateTime> startAt = createDateTime("startAt", java.time.LocalDateTime.class);

    public final EnumPath<GameStatus> status = createEnum("status", GameStatus.class);

    public final NumberPath<Double> thirdPlacePrize = createNumber("thirdPlacePrize", Double.class);

    public final StringPath thirdUserName = createString("thirdUserName");

    public final NumberPath<Double> totalPrize = createNumber("totalPrize", Double.class);

    public final EnumPath<GameType> type = createEnum("type", GameType.class);

    public QGame(String variable) {
        super(Game.class, forVariable(variable));
    }

    public QGame(Path<? extends Game> path) {
        super(path.getType(), path.getMetadata());
    }

    public QGame(PathMetadata metadata) {
        super(Game.class, metadata);
    }

}

