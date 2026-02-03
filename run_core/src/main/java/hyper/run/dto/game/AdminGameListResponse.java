package hyper.run.dto.game;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameDistance;
import hyper.run.domain.game.entity.GameType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminGameListResponse {
    private Long id;
    private String name;
    private GameType type;
    private String typeName;
    private GameDistance distance;
    private String distanceName;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String startAtFormatted;
    private String endAtFormatted;
    private int participatedCount;
    private double totalPrize;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdAtFormatted;
    private String updatedAtFormatted;
    private String status; // SCHEDULED, IN_PROGRESS, FINISHED

    public static AdminGameListResponse from(Game game) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime now = LocalDateTime.now();
        String status;

        if (now.isBefore(game.getStartAt())) {
            status = "SCHEDULED";
        } else if (now.isAfter(game.getEndAt())) {
            status = "FINISHED";
        } else {
            status = "IN_PROGRESS";
        }

        return AdminGameListResponse.builder()
                .id(game.getId())
                .name(game.getName())
                .type(game.getType())
                .typeName(game.getType().getName())
                .distance(game.getDistance())
                .distanceName(game.getDistance().getName())
                .startAt(game.getStartAt())
                .endAt(game.getEndAt())
                .startAtFormatted(game.getStartAt().format(formatter))
                .endAtFormatted(game.getEndAt().format(formatter))
                .participatedCount(game.getParticipatedCount())
                .totalPrize(game.getTotalPrize())
                .createdAt(game.getCreateDateTime())
                .updatedAt(game.getModifiedDateTime())
                .createdAtFormatted(game.getCreateDateTime().format(formatter))
                .updatedAtFormatted(game.getModifiedDateTime().format(formatter))
                .status(status)
                .build();
    }
}
