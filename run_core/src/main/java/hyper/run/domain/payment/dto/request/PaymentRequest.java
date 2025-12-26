package hyper.run.domain.payment.dto.request;

import hyper.run.domain.payment.entity.InAppType;
import hyper.run.domain.payment.entity.Payment;
import hyper.run.domain.payment.entity.PaymentState;
import hyper.run.domain.user.entity.User;
import lombok.Getter;


@Getter
public class PaymentRequest {

    private int price;

    private String transactionId;    // Apple transactionId 또는 Google orderId

    private String productId;        // point_100

    private String receiptData;      // Apple: Base64 영수증 데이터, Google: Purchase Token

    private int couponAmount;

    private String paymentMethod;

    private InAppType inAppType;

    public Payment toEntity(User user){
        return new Payment(price,
                couponAmount,
                inAppType,
                PaymentState.PENDING,
                paymentMethod,
                transactionId,
                productId,
                receiptData,
                user);
    }
}
