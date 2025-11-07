package hyper.run.domain.exchange_transaction.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QExchangeTransaction is a Querydsl query type for ExchangeTransaction
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QExchangeTransaction extends EntityPathBase<ExchangeTransaction> {

    private static final long serialVersionUID = -1879858409L;

    public static final QExchangeTransaction exchangeTransaction = new QExchangeTransaction("exchangeTransaction");

    public final hyper.run.domain.common.QBaseTimeEntity _super = new hyper.run.domain.common.QBaseTimeEntity(this);

    public final StringPath accountNumber = createString("accountNumber");

    public final NumberPath<Double> amount = createNumber("amount", Double.class);

    public final StringPath bankName = createString("bankName");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDateTime = _super.createDateTime;

    public final EnumPath<ExchangeStatus> exchangeStatus = createEnum("exchangeStatus", ExchangeStatus.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDateTime = _super.modifiedDateTime;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QExchangeTransaction(String variable) {
        super(ExchangeTransaction.class, forVariable(variable));
    }

    public QExchangeTransaction(Path<? extends ExchangeTransaction> path) {
        super(path.getType(), path.getMetadata());
    }

    public QExchangeTransaction(PathMetadata metadata) {
        super(ExchangeTransaction.class, metadata);
    }

}

