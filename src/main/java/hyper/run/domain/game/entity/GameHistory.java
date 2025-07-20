package hyper.run.domain.game.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 사용자 자신의 게임 기록(결과)
 */
@Document(collection = "game_history")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameHistory {

    @Id
    private String id;

    @Field("game_id")
    private Long gameId;

    @Field("user_id")
    private Long userId;

    @Field("rank")
    private int rank;

    @Field("prize")
    private Integer prize;

    @Field("average_bpm")
    private Integer averageBpm;

    @Field("target_cadence")
    private Integer targetCadence;

}