package hyper.run.domain.game.dto.response;

import hyper.run.domain.game.entity.GameStatus;
import hyper.run.domain.game.entity.GameType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class GameResponse {

    private Long id;

    private String name;

    private GameType type;

    private LocalDate gameDate;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private int participatedCount;

    private int firstPlacePrize;

    private int secondPlacePrize;

    private int thirdPlacePrize;

    private GameStatus status;
}
