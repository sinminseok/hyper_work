package hyper.run.domain.inquiry.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCustomerInquiry is a Querydsl query type for CustomerInquiry
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCustomerInquiry extends EntityPathBase<CustomerInquiry> {

    private static final long serialVersionUID = 1029644330L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCustomerInquiry customerInquiry = new QCustomerInquiry("customerInquiry");

    public final hyper.run.domain.common.QBaseTimeEntity _super = new hyper.run.domain.common.QBaseTimeEntity(this);

    public final StringPath accountNumber = createString("accountNumber");

    public final StringPath answer = createString("answer");

    public final StringPath bankName = createString("bankName");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDateTime = _super.createDateTime;

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath message = createString("message");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDateTime = _super.modifiedDateTime;

    public final NumberPath<Long> paymentId = createNumber("paymentId", Long.class);

    public final NumberPath<Integer> refundPrice = createNumber("refundPrice", Integer.class);

    public final EnumPath<RefundType> refundType = createEnum("refundType", RefundType.class);

    public final EnumPath<InquiryState> state = createEnum("state", InquiryState.class);

    public final StringPath title = createString("title");

    public final EnumPath<InquiryType> type = createEnum("type", InquiryType.class);

    public final hyper.run.domain.user.entity.QUser user;

    public QCustomerInquiry(String variable) {
        this(CustomerInquiry.class, forVariable(variable), INITS);
    }

    public QCustomerInquiry(Path<? extends CustomerInquiry> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCustomerInquiry(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCustomerInquiry(PathMetadata metadata, PathInits inits) {
        this(CustomerInquiry.class, metadata, inits);
    }

    public QCustomerInquiry(Class<? extends CustomerInquiry> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new hyper.run.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

