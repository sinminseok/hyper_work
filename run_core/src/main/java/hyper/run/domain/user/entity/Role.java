package hyper.run.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    GENERAL("ROLE_GENERAL"),
    ADMIN("ROLE_ADMIN");

    private final String key;

}
