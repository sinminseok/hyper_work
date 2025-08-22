package hyper.run.domain.dto.response;

import hyper.run.domain.user.entity.Role;

import java.util.Date;

public record AccessTokenPayload(String email, Role role, Date date) {
}
