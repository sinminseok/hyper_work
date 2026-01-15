package hyper.run.domain.user.event;

import hyper.run.domain.user.entity.User;
import lombok.Getter;

@Getter
public class UserCreateEvent {
    private final User user;

    public UserCreateEvent(User user) {
        this.user = user;
    }
}
