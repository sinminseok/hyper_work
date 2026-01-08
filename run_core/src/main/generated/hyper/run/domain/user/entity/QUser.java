package hyper.run.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 999084664L;

    public static final QUser user = new QUser("user");

    public final hyper.run.domain.common.QBaseTimeEntity _super = new hyper.run.domain.common.QBaseTimeEntity(this);

    public final StringPath birth = createString("birth");

    public final NumberPath<Integer> coupon = createNumber("coupon", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDateTime = _super.createDateTime;

    public final ListPath<hyper.run.domain.inquiry.entity.CustomerInquiry, hyper.run.domain.inquiry.entity.QCustomerInquiry> customerInquiries = this.<hyper.run.domain.inquiry.entity.CustomerInquiry, hyper.run.domain.inquiry.entity.QCustomerInquiry>createList("customerInquiries", hyper.run.domain.inquiry.entity.CustomerInquiry.class, hyper.run.domain.inquiry.entity.QCustomerInquiry.class, PathInits.DIRECT2);

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<hyper.run.domain.inquiry.entity.CustomerInquiry, hyper.run.domain.inquiry.entity.QCustomerInquiry> inquiries = this.<hyper.run.domain.inquiry.entity.CustomerInquiry, hyper.run.domain.inquiry.entity.QCustomerInquiry>createList("inquiries", hyper.run.domain.inquiry.entity.CustomerInquiry.class, hyper.run.domain.inquiry.entity.QCustomerInquiry.class, PathInits.DIRECT2);

    public final EnumPath<LoginType> loginType = createEnum("loginType", LoginType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDateTime = _super.modifiedDateTime;

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    public final ListPath<hyper.run.domain.payment.entity.Payment, hyper.run.domain.payment.entity.QPayment> payments = this.<hyper.run.domain.payment.entity.Payment, hyper.run.domain.payment.entity.QPayment>createList("payments", hyper.run.domain.payment.entity.Payment.class, hyper.run.domain.payment.entity.QPayment.class, PathInits.DIRECT2);

    public final StringPath phoneNumber = createString("phoneNumber");

    public final NumberPath<Double> point = createNumber("point", Double.class);

    public final StringPath profileUrl = createString("profileUrl");

    public final StringPath refreshToken = createString("refreshToken");

    public final StringPath watchConnectedKey = createString("watchConnectedKey");

    public final StringPath watchRefreshToken = createString("watchRefreshToken");

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

