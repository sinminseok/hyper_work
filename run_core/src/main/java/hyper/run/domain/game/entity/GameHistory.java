package hyper.run.domain.game.entity;

import hyper.run.domain.game.dto.request.GameHistoryUpdateRequest;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 사용자 자신의 게임 기록(결과)
 */
@Document(collection = "game_history")
@CompoundIndex(name = "game_user_idx", def = "{'game_id': 1, 'user_id': 1}")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameHistory {

    @Id
    private String id;

    @Field("game_id")
    private Long gameId;

    @Field("game_distance")
    private GameDistance gameDistance;

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

    @Field("current_speed")
    @Setter
    private double currentSpeed;

    @Field("current_vertical_oscillation")
    @Setter
    private double currentVerticalOscillation;

    @Field("update_count")
    private int updateCount;

    @Field("is_done")
    @Setter
    private boolean done;

    @Field("is_connected_watch")
    @Setter
    private boolean connectedWatch;

    public void checkDoneGameByDistance(){
        if(currentDistance >= gameDistance.getDistance()){
            this.done = true;
        }
    }

    //남은거리가 짧을수록 높은 순위
    public double calculateRemainDistance() {
        // gameDistance.getDistance() 는 단위가 m
        return gameDistance.getDistance() - currentDistance;
    }

    public double calculateCadenceScore(){
        return Math.abs(targetCadence - currentCadence);
    }

    public double calculateHeartBeatScore() {
        return Math.abs(targetBpm - currentBpm);
    }

    public void updateCurrentValue(GameHistoryUpdateRequest request) {
        updateCount++; // 업데이트 횟수 증가
        // 누적 합산 (예: 총 거리)
        currentDistance = request.getCurrentDistance();

        //currentSpeed = ((currentSpeed * (updateCount - 1)) + request.getCurrentSpeed()) / updateCount; // 정해진 거리 빠르게 도착할 수록 승리

        currentBpm = ((currentBpm * (updateCount - 1)) + request.getCurrentBpm()) / updateCount; // 목표 평균값에 가까울수록 우승
        currentCadence = ((currentCadence * (updateCount - 1)) + request.getCurrentCadence()) / updateCount; // 목표 평균값에 가까울수록 우승
        currentFlightTime += request.getCurrentFlightTime(); // 누적값이 커야 우승
        currentPower = ((currentPower * (updateCount - 1)) + request.getCurrentPower()) / updateCount; // 평균값이 높아야 우승
        currentGroundContactTime = ((currentGroundContactTime * (updateCount - 1)) + request.getCurrentGroundContactTime()) / updateCount; // 평균값이 낮아야 우승
        currentVerticalOscillation = ((currentVerticalOscillation * (updateCount - 1)) + request.getCurrentVerticalOscillation()) / updateCount; // 평균값이 낮아야 우승
    }
}