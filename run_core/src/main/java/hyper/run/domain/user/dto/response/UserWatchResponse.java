package hyper.run.domain.user.dto.response;

import hyper.run.domain.user.entity.UserWatch;
import hyper.run.domain.user.entity.WatchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWatchResponse {

    private boolean canGCT;

    private boolean canVerticalOscillation;

    private boolean canPower;

    private double weight;

    private WatchType watchType;

    public static UserWatchResponse from(UserWatch userWatch) {
        return UserWatchResponse.builder()
                .canGCT(userWatch.isCanGCT())
                .canVerticalOscillation(userWatch.isCanVerticalOscillation())
                .canPower(userWatch.isCanPower())
                .weight(userWatch.getWeight())
                .watchType(userWatch.getWatchType())
                .build();
    }
}
