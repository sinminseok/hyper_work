package hyper.run.domain.game.service.scheduler;

import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.game.service.GameRankService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class GameGenerateScheduler {

    private final Map<GameType, GameRankService> gameRankServices;

    /**
     * 서비스 시작할때만 일주일치 모두 생성
     * 이후에 다음주 경기만 생성 ex) 5/1 일이면 5/8일 경기만 생성
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void generateGames() {
        for(GameType type : GameType.values()) {
            // gameRankServices.get(type).generateGame(생성할 날짜, 총 상금);
        }
    }
}
