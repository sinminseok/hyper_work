package hyper.run.domain.payment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAppleTransaction is a Querydsl query type for AppleTransaction
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAppleTransaction extends EntityPathBase<AppleTransaction> {

    private static final long serialVersionUID = 1141433954L;

    public static final QAppleTransaction appleTransaction = new QAppleTransaction("appleTransaction");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath productId = createString("productId");

    public final DateTimePath<java.time.Instant> purchasedAt = createDateTime("purchasedAt", java.time.Instant.class);

    public final StringPath transactionId = createString("transactionId");

    public QAppleTransaction(String variable) {
        super(AppleTransaction.class, forVariable(variable));
    }

    public QAppleTransaction(Path<? extends AppleTransaction> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAppleTransaction(PathMetadata metadata) {
        super(AppleTransaction.class, metadata);
    }

}

