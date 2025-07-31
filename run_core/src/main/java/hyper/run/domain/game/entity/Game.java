package hyper.run.domain.game.entity;

import hyper.run.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

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

    private String name; // 경기 이름

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private GameType type; // 경기 유형

    @Enumerated(EnumType.STRING)
    @Column(name = "distance", nullable = false)
    private GameDistance distance; // 경기 거리

    @Column(name = "game_date")
    private LocalDate gameDate; // 경기 날짜

    @Column(name = "start_at")
    private LocalDateTime startAt; // 경기 시작 시간

    @Column(name = "end_at")
    private LocalDateTime endAt; // 경기 종료 시간

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
    @Setter
    private String firstUserName; // 우승자 이름

    @Column(name = "second_user_name", nullable = true)
    @Setter
    private String secondUserName; // 2등 이름

    @Column(name = "third_user_name", nullable = true)
    @Setter
    private String thirdUserName; // 3등 이름

    //전체 참가 인원 증가
    public void increaseParticipatedCount(){
        this.participatedCount += 1;
    }

    //전체 참가 인원 감소
    public void decreaseParticipatedCount(){
        this.participatedCount -= 1;
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
}
