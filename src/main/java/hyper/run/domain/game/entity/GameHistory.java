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
    @Setter
    private int rank;

    @Field("prize")
    @Setter
    private double prize;

    @Field("target_bpm")
    private Integer targetBpm;

    @Field("target_cadence")
    private Integer targetCadence;

    @Field("current_bpm")
    @Setter
    private double currentBpm;

    @Field("current_cadence")
    @Setter
    private double currentCadence;

    @Field("current_distance")
    @Setter
    private double currentDistance;

    @Field("current_flight_time")
    @Setter
    private double currentFlightTime;

    @Field("current_ground_contact_time")
    @Setter
    private double currentGroundContactTime;

    @Field("current_power")
    @Setter
    private double currentPower;

    @Field("current_vertical_oscillation")
    @Setter
    private double currentVerticalOscillation;


    public double calculateCadenceScore(){
        return Math.abs(targetCadence - currentCadence);
    }

    public double calculateHeartBeatScore() {
        return Math.abs(targetBpm - currentBpm);
    }
}