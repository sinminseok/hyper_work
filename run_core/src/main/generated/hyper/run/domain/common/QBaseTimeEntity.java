package hyper.run.domain.common;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBaseTimeEntity is a Querydsl query type for BaseTimeEntity
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QBaseTimeEntity extends EntityPathBase<BaseTimeEntity<?>> {

    private static final long serialVersionUID = 830352223L;

    public static final QBaseTimeEntity baseTimeEntity = new QBaseTimeEntity("baseTimeEntity");

    public final DateTimePath<java.time.LocalDateTime> createDateTime = createDateTime("createDateTime", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> modifiedDateTime = createDateTime("modifiedDateTime", java.time.LocalDateTime.class);

    @SuppressWarnings({"all", "rawtypes", "unchecked"})
    public QBaseTimeEntity(String variable) {
        super((Class) BaseTimeEntity.class, forVariable(variable));
    }

    @SuppressWarnings({"all", "rawtypes", "unchecked"})
    public QBaseTimeEntity(Path<? extends BaseTimeEntity> path) {
        super((Class) path.getType(), path.getMetadata());
    }

    @SuppressWarnings({"all", "rawtypes", "unchecked"})
    public QBaseTimeEntity(PathMetadata metadata) {
        super((Class) BaseTimeEntity.class, metadata);
    }

}

