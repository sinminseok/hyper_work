package hyper.run.domain.dto.response;


import java.util.Date;

public record RefreshTokenPayload(String email, Date date) {
}
