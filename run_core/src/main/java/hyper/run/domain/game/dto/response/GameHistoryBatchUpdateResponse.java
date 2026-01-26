package hyper.run.domain.game.dto.response;

import hyper.run.domain.game.entity.GameHistory;
import lombok.Builder;
import lombok.Getter;

/**
 * Apple Watch용 HTTP Polling 최적화를 위한 배치 업데이트 응답 DTO
 * 동적 폴링 주기를 클라이언트에 전달하여 트래픽 최적화
 */
@Getter
@Builder
public class GameHistoryBatchUpdateResponse {

    private boolean success;

    /**
     * 다음 폴링 주기 (초)
     * -1: 경기 완료, 더 이상 폴링 불필요
     * 3: 완주 임박 (90% 이상)
     * 5: 경기 시작 직후 (10% 이하)
     * 10: 일반 진행 중
     */
    private int nextPollIntervalSeconds;

    /**
     * 경기 완료 여부
     */
    private boolean isDone;

    /**
     * 현재 진행률 (0.0 ~ 1.0)
     */
    private double progress;

    /**
     * 현재 거리 (미터)
     */
    private double currentDistance;

    /**
     * 목표 거리 (미터)
     */
    private double targetDistance;

    public static GameHistoryBatchUpdateResponse of(GameHistory history) {
        double targetDistance = history.getGameDistance().getDistance();
        double currentDistance = history.getCurrentDistance();
        double progress = targetDistance > 0 ? currentDistance / targetDistance : 0;
        int interval = calculateInterval(history, progress);

        return GameHistoryBatchUpdateResponse.builder()
                .success(true)
                .nextPollIntervalSeconds(interval)
                .isDone(history.isDone())
                .progress(progress)
                .currentDistance(currentDistance)
                .targetDistance(targetDistance)
                .build();
    }

    private static int calculateInterval(GameHistory history, double progress) {
        if (history.isDone()) {
            return -1;  // 더 이상 폴링 불필요
        }

        if (progress >= 0.9) {
            return 3;   // 완주 임박
        } else if (progress <= 0.1) {
            return 5;   // 시작 직후
        } else {
            return 10;  // 일반 진행
        }
    }
}
