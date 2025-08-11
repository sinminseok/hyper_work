package hyper.run.domain.game.dto.response;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminGameResponse {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private GameStatus status;

    public static AdminGameResponse gamesToAdminGamesDto(Game game){
        return AdminGameResponse.builder()
                .id(game.getId())
                .name(game.getName())
                .createdAt(game.getCreatedAt())
                .modifiedAt(game.getModifiedAt())
                .status(determineStatus(game.getStartAt(),game.getEndAt()))
                .build();
    }

    private static GameStatus determineStatus(LocalDateTime startAt, LocalDateTime endAt) {
        LocalDateTime now = LocalDateTime.now();
        // 예정 경기
        if (now.isBefore(startAt)) {
            return GameStatus.SCHEDULED;
        }
        // 진행 경기
        else if (now.isAfter(startAt) && now.isBefore(endAt)) {
            return GameStatus.PROGRESS;
        }
        // 종료 경기
        else if (now.isAfter(endAt)) {
            return GameStatus.FINISHED;
        }
        return null;
    }

}
