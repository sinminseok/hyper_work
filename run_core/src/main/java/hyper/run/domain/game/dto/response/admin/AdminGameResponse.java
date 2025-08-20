package hyper.run.domain.game.dto.response.admin;

import hyper.run.domain.game.entity.AdminGameStatus;
import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private AdminGameStatus adminGameStatus;

    public static AdminGameResponse dtoFromGame(Game game) {
        return AdminGameResponse.builder()
                .id(game.getId())
                .name(game.getName())
                .createdAt(game.getCreatedAt())
                .modifiedAt(game.getModifiedAt())
                .adminGameStatus(game.getAdminGameStatus())
                .build();
    }
}
