package hyper.run.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserWatch is a Querydsl query type for UserWatch
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserWatch extends EntityPathBase<UserWatch> {

    private static final long serialVersionUID = 1197722935L;

    public static final QUserWatch userWatch = new QUserWatch("userWatch");

    public final BooleanPath canGCT = createBoolean("canGCT");

    public final BooleanPath canPower = createBoolean("canPower");

    public final BooleanPath canVerticalOscillation = createBoolean("canVerticalOscillation");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final EnumPath<WatchType> watchType = createEnum("watchType", WatchType.class);

    public final NumberPath<Double> weight = createNumber("weight", Double.class);

    public QUserWatch(String variable) {
        super(UserWatch.class, forVariable(variable));
    }

    public QUserWatch(Path<? extends UserWatch> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserWatch(PathMetadata metadata) {
        super(UserWatch.class, metadata);
    }

}

