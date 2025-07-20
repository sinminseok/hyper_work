package hyper.run.domain.game.entity;

import hyper.run.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "game")
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Game {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id", updatable = false)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private GameType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "distance", nullable = false)
    private GameDistance distance;

    @Column(name = "game_date")
    private LocalDate gameDate;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "participated_count")
    private int participatedCount; // 총 참여 인원

    @Column(name = "total_prize")
    private double totalPrize; // 총 상금

    @Column(name = "first_place_prize")
    private double firstPlacePrize; // 우승 상금 (총상금의 80%)

    @Column(name = "second_place_prize")
    private double secondPlacePrize; // 2등 상금 (총상금의 15%)

    @Column(name = "third_place_prize")
    private double thirdPlacePrize; // 3등 상금 (총상금의 5%)

    @Column(name = "first_user_name", nullable = true)
    private String firstUserName; // 우승자 이름

    @Column(name = "second_user_name", nullable = true)
    private String secondUserName; // 2등 이름

    @Column(name = "third_user_name", nullable = true)
    private String thirdUserName; // 3등 이름


    public void increaseParticipatedCount(){
        this.participatedCount += 1;
    }

    public void decreaseParticipatedCount(){
        this.participatedCount -= 1;
    }

    // 현재 시간이 startAt과 endAt 사이에 포함되어 있는지 확인
    public boolean isInProgress() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startAt) && now.isBefore(endAt);
    }

    // 현재 시간이 startAt 이전인지 확인
    public boolean isNotYetStart() {
        LocalDateTime now = LocalDateTime.now();
        return now.isBefore(startAt);
    }

}
