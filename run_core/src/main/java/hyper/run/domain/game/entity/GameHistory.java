package hyper.run.domain.game.entity;

import hyper.run.domain.game.dto.request.GameApplyRequest;
import hyper.run.domain.game.dto.request.GameHistoryUpdateRequest;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;


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
    private int targetBpm;

    @Field("target_cadence")
    private int targetCadence;

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
    @Field("start_at")
    private LocalDateTime startAt; // 사용자가 경기를 시작한 시간 (초기값은 Game 의 startAt 참고)

    @Setter
    @Field("end_at")
    private LocalDateTime endAt; // 사용자가 경기를 종료한 시간 (초기값은 Game 의 endAt 참고)

    @Setter
    @Field("average_bpm")
    private double average_bpm;

    @Setter
    @Field("average_cadence")
    private double average_cadence;

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
        if (!this.done) {  // 처음 완주할 때만 endAt 기록
            this.done = true;
            this.endAt = LocalDateTime.now();
        }
    }

    public void connectWatch() {
        this.connectedWatch = true;
    }

    public static GameHistory createForApply(Long gameId, Long userId, GameDistance gameDistance, Integer averageBpm, Integer targetCadence, LocalDateTime gameStartAt, LocalDateTime gameEndAt) {
        GameHistoryBuilder builder = GameHistory.builder()
                .gameId(gameId)
                .userId(userId)
                .gameDistance(gameDistance)
                .prize(0)
                .updateCount(0)
                .done(false)
                .rank(0)
                .startAt(gameStartAt)
                .endAt(gameEndAt);

        if (averageBpm != null) {
            builder.targetBpm(averageBpm);
        }
        if (targetCadence != null) {
            builder.targetCadence(targetCadence);
        }

        return builder.build();
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
     * 경기 소요 시간 계산 (초 단위)
     * startAt과 endAt의 차이를 계산
     * 완주하지 않은 경우 Long.MAX_VALUE 반환
     */
    public long getDurationInSeconds() {
        if (startAt == null || endAt == null || !done) {
            return Long.MAX_VALUE;  // 미완주자는 가장 낮은 순위
        }
        return java.time.Duration.between(startAt, endAt).getSeconds();
    }

    /**
     * getCurrentDistance() 거리 데이터를 수집할때, 워치에서, 현재 이동 거리를 시작을 감지해 측정한다.
     */
    public void updateFrom(GameHistoryUpdateRequest request) {
        updateCount++;
        int previousCount = updateCount - 1;

        // 첫 업데이트 시 실제 경기 시작 시간 기록
        if (updateCount == 1) {
            this.startAt = LocalDateTime.now();
        }

        currentDistance = request.getCurrentDistance();
        currentBpm = calculateAverage(currentBpm, request.getCurrentBpm(), previousCount);
        currentCadence = calculateAverage(currentCadence, request.getCurrentCadence(), previousCount);
        currentPower = calculateAverage(currentPower, request.getCurrentPower(), previousCount);
        currentGroundContactTime = calculateAverage(currentGroundContactTime, request.getCurrentGroundContactTime(), previousCount);
        currentVerticalOscillation = calculateAverage(currentVerticalOscillation, request.getCurrentVerticalOscillation(), previousCount);
        accumulateFlightTime(request.getCurrentFlightTime());
    }

    private double calculateAverage(double currentValue, double newValue, int previousCount) {
        if(newValue == 0.0){
            return currentValue;
        }
        return ((currentValue * previousCount) + newValue) / updateCount;
    }

    private void accumulateFlightTime(double additionalTime) {
        currentFlightTime += additionalTime;
    }
}
