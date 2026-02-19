package hyper.run.domain.user.dto.request;

import hyper.run.domain.user.entity.UserWatch;
import hyper.run.domain.user.entity.WatchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWatchRegisterRequest {

    private boolean canGCT;

    private boolean canVerticalOscillation;

    private boolean canPower;

    private double weight;

    private WatchType watchType;

    public UserWatch toEntity(Long userId) {
        return UserWatch.builder()
                .userId(userId)
                .canGCT(this.canGCT)
                .canVerticalOscillation(this.canVerticalOscillation)
                .canPower(this.canPower)
                .weight(this.weight)
                .watchType(this.watchType)
                .build();
    }
}
