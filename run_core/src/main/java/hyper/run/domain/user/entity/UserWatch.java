package hyper.run.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "user_watch", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_device", columnNames = {"user_id", "device_id"})
})
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

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    private WatchType watchType;

    //여기서부터는 일단 사용 x 나중에 확장성을 위해 남겨둠

    @Column(name = "can_gct")
    private boolean canGCT;

    @Column(name = "can_vertical_oscillation")
    private boolean canVerticalOscillation;

    @Column(name = "power")
    private boolean canPower;

    @Column(name = "weight", nullable = true)
    private double weight;

    public void updateFrom(UserWatch other) {
        this.name = other.getName();
        this.canGCT = other.isCanGCT();
        this.canVerticalOscillation = other.isCanVerticalOscillation();
        this.canPower = other.isCanPower();
        this.weight = other.getWeight();
        this.watchType = other.getWatchType();
    }
}
