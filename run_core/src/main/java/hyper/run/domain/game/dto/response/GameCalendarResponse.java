package hyper.run.domain.game.dto.response;

import hyper.run.domain.game.entity.GameDistance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameCalendarResponse {
    private Long gameId;

    private GameDistance gameDistance;

    private int participatedCount;

    private LocalDateTime gameStartAt; //경기 시작 시간 (Game 의 startAt)

    private LocalDateTime gameEndAt; // 경기 종료 시간 (Game 의 endAt)

    private LocalDateTime userStartAt; // 실제 사용자가 참여한 시간 (GameHistory 의 startAt;

    private LocalDateTime userEndAt; // 실제 사용자가 참여한 시간 (GameHistory 의 endAt);

    private double firstPlacePrize;

    private double secondPlacePrize;

    private double thirdPlacePrize;

    private double myDistance; // 실제 이동 거리(GameHistory 의 currentDistance)

}
