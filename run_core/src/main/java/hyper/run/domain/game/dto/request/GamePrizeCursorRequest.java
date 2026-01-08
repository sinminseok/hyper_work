package hyper.run.domain.game.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GamePrizeCursorRequest {

    private Double cursorTotalPrize;

    private Long cursorGameId;

    private Integer size = 10;

    public boolean hasCursor() {
        return cursorTotalPrize != null && cursorGameId != null;
    }

    public static GamePrizeCursorRequest of(Double cursorTotalPrize, Long cursorGameId, Integer size) {
        return new GamePrizeCursorRequest(cursorTotalPrize, cursorGameId, size != null ? size : 10);
    }
}
