package hyper.run.domain.game.dto.request;

import hyper.run.domain.game.entity.ConnectType;
import lombok.Getter;

@Getter
public class GameStartRequest {

    private Long gameId;

    private ConnectType connectType;
}
