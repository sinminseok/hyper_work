package hyper.run.domain.user.dto.response;

import hyper.run.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentUserResponse {

    private String name;
    private String phoneNumber;
    private String email;

    public static PaymentUserResponse userToPayment(User user){
        return PaymentUserResponse.builder()
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .build();
    }
}
