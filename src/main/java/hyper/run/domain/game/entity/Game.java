package hyper.run.domain.game.entity;

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

    private LocalDate gameDate;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @Column(name = "participated_count")
    private int participatedCount;
}
