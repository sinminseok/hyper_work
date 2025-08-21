package hyper.run.domain.game.entity;

import hyper.run.domain.game.dto.request.GameHistoryUpdateRequest;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "game_history")
@CompoundIndex(name = "game_user_idx", def = "{'game_id': 1, 'user_id': 1}")
@CompoundIndex(name = "user_id_idx", def = "{'user_id': 1}")
public class GameHistory {

    @Id
    private String id;

    @Field("game_id")
    private Long gameId;

    @Field("game_distance")
    private GameDistance gameDistance;

    @Field("user_id")
    private Long userId;

    @Setter
    @Field("rank")
    private int rank;

    @Setter
    @Field("prize")
    private double prize;

    @Field("target_bpm")
    private Integer targetBpm;

    @Field("target_cadence")
    private Integer targetCadence;

    @Setter
    @Field("current_bpm")
    private double currentBpm;

    @Setter
    @Field("current_cadence")
    private double currentCadence;

    @Setter
    @Field("current_distance")
    private double currentDistance;

    @Setter
    @Field("current_flight_time")
    private double currentFlightTime;

    @Setter
    @Field("current_ground_contact_time")
    private double currentGroundContactTime;

    @Setter
    @Field("current_power")
    private double currentPower;

    @Setter
    @Field("current_speed")
    private double currentSpeed;

    @Setter
    @Field("current_vertical_oscillation")
    private double currentVerticalOscillation;

    @Field("update_count")
    private int updateCount;

    @Setter
    @Field("is_done")
    private boolean done;

    @Setter
    @Field("is_connected_watch")
    private boolean connectedWatch;

    public void checkDoneByDistance() {
        if (currentDistance >= gameDistance.getDistance()) {
            markAsDone();
        }
    }

    public void markAsDone() {
        this.done = true;
    }

    public void connectWatch() {
        this.connectedWatch = true;
    }

    public double getRemainingDistance() {
        return gameDistance.getDistance() - currentDistance;
    }

    public double getCadenceScore() {
        return Math.abs(targetCadence - currentCadence);
    }

    public double getHeartBeatScore() {
        return Math.abs(targetBpm - currentBpm);
    }

    /**
     * getCurrentDistance() 거리 데이터를 수집할때, 워치에서, 현재 이동 거리를 시작을 감지해 측정한다.
     */
    public void updateFrom(GameHistoryUpdateRequest request) {
        updateCount++;
        int previousCount = updateCount - 1;
        currentDistance = request.getCurrentDistance();
        currentBpm = calculateAverage(currentBpm, request.getCurrentBpm(), previousCount);
        currentCadence = calculateAverage(currentCadence, request.getCurrentCadence(), previousCount);
        currentPower = calculateAverage(currentPower, request.getCurrentPower(), previousCount);
        currentGroundContactTime = calculateAverage(currentGroundContactTime, request.getCurrentGroundContactTime(), previousCount);
        currentVerticalOscillation = calculateAverage(currentVerticalOscillation, request.getCurrentVerticalOscillation(), previousCount);
        accumulateFlightTime(request.getCurrentFlightTime());
    }

    private double calculateAverage(double currentValue, double newValue, int previousCount) {
        return ((currentValue * previousCount) + newValue) / updateCount;
    }

    private void accumulateFlightTime(double additionalTime) {
        currentFlightTime += additionalTime;
    }
}
