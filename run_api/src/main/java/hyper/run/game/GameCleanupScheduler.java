package hyper.run.game;

import hyper.run.domain.game.service.GameManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameCleanupScheduler {

    private final GameManagementService gameManagementService;

    /**
     * 매일 자정에 만료된 경기 중 참여자가 0인 경기를 삭제한다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupExpiredGames() {
        log.info("만료된 경기 정리 스케줄러 시작");
        gameManagementService.cleanupExpiredGamesWithNoParticipants();
        log.info("만료된 경기 정리 스케줄러 완료");
    }
}
