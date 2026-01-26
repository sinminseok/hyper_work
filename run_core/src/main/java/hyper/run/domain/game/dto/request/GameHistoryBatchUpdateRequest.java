package hyper.run.domain.game.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Apple Watch용 HTTP Polling 최적화를 위한 배치 업데이트 요청 DTO
 * 클라이언트에서 여러 생체 데이터 샘플을 모아서 한 번에 전송할 때 사용
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameHistoryBatchUpdateRequest {

    private Long gameId;

    private Long userId;

    private List<BioDataSample> samples;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BioDataSample {
        private double currentBpm;
        private double currentCadence;
        private double currentDistance;
        private double currentFlightTime;
        private double currentGroundContactTime;
        private double currentPower;
        private double currentVerticalOscillation;
        private double currentSpeed;
    }
}
