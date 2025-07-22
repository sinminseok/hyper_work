package hyper.run.domain.user.dto.request;

import lombok.Getter;

@Getter
public class UserUpdateRequest {
    private String name;

    private String phoneNumber;

    private String brith;
}
