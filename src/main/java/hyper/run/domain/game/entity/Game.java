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
    private int participatedCount;

    @Column(name = "first_place_prize")
    private int firstPlacePrize;

    @Column(name = "second_place_prize")
    private int secondPlacePrize;

    @Column(name = "third_place_prize")
    private int thirdPlacePrize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
