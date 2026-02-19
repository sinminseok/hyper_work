package hyper.run.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "user_watch")
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserWatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_watch_id", updatable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "can_gct")
    private boolean canGCT;

    @Column(name = "can_vertical_oscillation")
    private boolean canVerticalOscillation;

    @Column(name = "power")
    private boolean canPower;

    @Column(name = "weight", nullable = true)
    private double weight;

    @Enumerated(EnumType.STRING)
    private WatchType watchType;
}
