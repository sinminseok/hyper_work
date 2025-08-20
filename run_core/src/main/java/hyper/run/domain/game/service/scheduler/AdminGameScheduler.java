package hyper.run.domain.game.service.scheduler;

import hyper.run.domain.game.entity.AdminGameStatus;
import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.repository.GameRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class AdminGameScheduler {

    private final GameRepository gameRepository;

    /**
     * 1분마다 실행되어 예정상태의 경기 중 시작 시간이 지난 경기를 진행으로 변경
     */
    @Scheduled(cron = "0 * * * * *") // 매 분 0초에 실행
    @Transactional
    public void updateScheduledToProgress() {
        LocalDateTime now = LocalDateTime.now();

        // 시작 시간이 지났지만 여전히 '예정' 상태인 경기들을 조회
        List<Game> gamesToStart = gameRepository.findAllByAdminGameStatusAndEndAtBefore(AdminGameStatus.SCHEDULED, now);

        if (gamesToStart.isEmpty()) {
            log.info("'진행중'으로 변경할 경기가 없습니다.");
            return;
        }

        for (Game game : gamesToStart) {
            game.updateAdminGameStatus(AdminGameStatus.PROGRESS);
            log.info("경기 상태 변경: ID {}번 '{}' -> PROGRESS", game.getId(), game.getName());
        }

    }

    /**
     * 1분마다 실행되어 진행중 상태의 경기 중 종료 시간이 지난 경기를 종료로 변경
     */
    @Scheduled(cron = "0 * * * * *") // 매 분 0초에 실행
    @Transactional
    public void updateProgressToFinished() {
        LocalDateTime now = LocalDateTime.now();

        // 종료 시간이 지났지만 여전히 진행중인 경기들을 조회
        List<Game> gamesToEnd = gameRepository.findAllByAdminGameStatusAndEndAtBefore(AdminGameStatus.PROGRESS, now);

        if (gamesToEnd.isEmpty()) {
            log.info("'종료'로 변경할 경기가 없습니다.");
            return;
        }

        for (Game game : gamesToEnd) {
            game.updateAdminGameStatus(AdminGameStatus.FINISHED);
            log.info("경기 상태 변경: ID {}번 '{}' -> FINISHED", game.getId(), game.getName());
        }
    }
}
