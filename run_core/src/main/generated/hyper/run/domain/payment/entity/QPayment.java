package hyper.run.domain.payment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPayment is a Querydsl query type for Payment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPayment extends EntityPathBase<Payment> {

    private static final long serialVersionUID = -748467960L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPayment payment = new QPayment("payment");

    public final hyper.run.domain.common.QBaseTimeEntity _super = new hyper.run.domain.common.QBaseTimeEntity(this);

    public final NumberPath<Integer> couponAmount = createNumber("couponAmount", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDateTime = _super.createDateTime;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<InAppType> inAppType = createEnum("inAppType", InAppType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDateTime = _super.modifiedDateTime;

    public final StringPath paymentMethod = createString("paymentMethod");

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final StringPath productId = createString("productId");

    public final StringPath receiptData = createString("receiptData");

    public final EnumPath<PaymentState> state = createEnum("state", PaymentState.class);

    public final StringPath transactionId = createString("transactionId");

    public final hyper.run.domain.user.entity.QUser user;

    public QPayment(String variable) {
        this(Payment.class, forVariable(variable), INITS);
    }

    public QPayment(Path<? extends Payment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPayment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPayment(PathMetadata metadata, PathInits inits) {
        this(Payment.class, metadata, inits);
    }

    public QPayment(Class<? extends Payment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new hyper.run.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

