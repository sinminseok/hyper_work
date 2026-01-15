package hyper.run.domain.user.event;

import lombok.Getter;

@Getter
public class UserEditEvent {
    private final Long userId;
    private final String name;
    private final String phoneNumber;
    private final String birth;

    public UserEditEvent(Long userId, String name, String phoneNumber, String birth) {
        this.userId = userId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.birth = birth;
    }
}
