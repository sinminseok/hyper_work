package hyper.run.domain.game.entity;

import hyper.run.domain.common.BaseTimeEntity;
import hyper.run.domain.game.event.GameApplyEvent;
import hyper.run.domain.game.event.GameCancelEvent;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(
        name = "game",
        indexes = {
                @Index(name = "idx_game_end_at", columnList = "end_at")
        }
)
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Game extends BaseTimeEntity<Game> {

    private static final int PARTICIPATION_FEE = 2500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id", updatable = false)
    private Long id;

    private String name; // 경기 이름

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private GameType type; // 경기 유형

    @Enumerated(EnumType.STRING)
    @Column(name = "active_type", nullable = false)
    private ActivityType activityType; // 걷기, 뛰기 타입 구분

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private GameStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_status")
    private AdminGameStatus adminGameStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "distance", nullable = false)
    private GameDistance distance; // 경기 거리

    @Column(name = "start_at")
    private LocalDateTime startAt; // 경기 시작 시간

    @Column(name = "end_at")
    private LocalDateTime endAt; // 경기 종료 시간

    @Column(name = "participated_count")
    private int participatedCount; // 총 참여 인원

    @Column(name = "total_prize")
    private Double totalPrize; // 총 상금

    @Column(name = "first_place_prize")
    private Double firstPlacePrize; // 우승 상금 (총상금의 80%)

    @Column(name = "second_place_prize")
    private Double secondPlacePrize; // 2등 상금 (총상금의 15%)

    @Column(name = "third_place_prize")
    private Double thirdPlacePrize; // 3등 상금 (총상금의 6%)

    @Column(name = "fourth_place_prize")
    private Double fourthPlacePrize; // 4등 상금 (총상금의 3%)

    @Column(name = "other_place_prize")
    private Double otherPlacePrize; // 5~20등 상금 (총상금의 1% 각)

    @Column(name = "first_user_name", nullable = true)
    @Setter
    private String firstUserName; // 우승자 이름

    @Column(name = "second_user_name", nullable = true)
    @Setter
    private String secondUserName; // 2등 이름

    @Column(name = "third_user_name", nullable = true)
    @Setter
    private String thirdUserName; // 3등 이름

    //참가 신청 이벤트 발행
    public void applyGame(Long userId, Integer averageBpm, Integer targetCadence) {
        increaseParticipatedCount();
        registerEvent(GameApplyEvent.from(userId, this.getId(), this.getDistance(), averageBpm, targetCadence, this.getStartAt(), this.getEndAt()));
    }

    // 전체 참가 인원 증가
    public void increaseParticipatedCount() {
        this.participatedCount += 1;
        updatePrizeByParticipants();
    }

    // 전체 참가 인원 감소
    public void decreaseParticipatedCount() {
        if (this.participatedCount > 0) {
            this.participatedCount -= 1;
            updatePrizeByParticipants();
        }
    }

    // 참가 인원 수에 따라 상금 재계산
    private void updatePrizeByParticipants() {
        this.totalPrize = (double) (this.participatedCount * PARTICIPATION_FEE);
        this.firstPlacePrize = Math.floor(totalPrize * 0.60);
        this.secondPlacePrize = Math.floor(totalPrize * 0.15);
        this.thirdPlacePrize = Math.floor(totalPrize * 0.06);
        this.fourthPlacePrize = Math.floor(totalPrize * 0.03);
        this.otherPlacePrize = Math.floor(totalPrize * 0.01);
    }

    public boolean canNotStartGame() {
        return this.participatedCount == 0;
    }

    // 경기 진행 여부 확인
    public boolean isInProgress() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startAt) && now.isBefore(endAt);
    }

    // 경기 시작 여부 확인
    public boolean isNotYetStart() {
        LocalDateTime now = LocalDateTime.now();
        return now.isBefore(startAt);
    }

    //참가 철회
    public void cancelGame(Long userId) {
        decreaseParticipatedCount();
        registerEvent(GameCancelEvent.from(userId, this.getId()));
    }

    // 경기 상태 업데이트
    public void updateAdminGameStatus(AdminGameStatus newStatus) {
        this.adminGameStatus = newStatus;
    }
}
