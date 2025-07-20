package hyper.run.domain.game.service.impl;

import hyper.run.domain.game.dto.response.RankResponse;
import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameDistance;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.domain.game.service.GameRankService;
import hyper.run.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static hyper.run.domain.game.utils.GamePrizeCalculator.*;

/**
 * 사용자가 설정한 케이던스(발걸음) 에 가장 근접할 수록 높은 점수
 */
@RequiredArgsConstructor
@Service
public class CadenceRankService implements GameRankService {

    private final GameRepository gameRepository;
    private final GameHistoryRepository gameHistoryRepository;

    //30초마다 자동 갱신
    //해당 메서드에서 각각 참여한 모든 사용자의 rank 를 최신순으로 나타낸다.
    //Game 의 1,2,3 등은 게임 종료후 갱신힌다.
    @Override
    public RankResponse calculateRank(Long gameId) {
        //하나의 경기에 참여한 모든 사용자를 비교해 순위를 비교해야 한다.

        return null;
    }

    @Override
    public void generateGame(LocalDate date, double totalPrize) {
        for(GameDistance distance : GameDistance.values()) {
            //i 는 시간
            for(int i=5; i <= 23; i += distance.getTime()){
                if(i + distance.getTime() > 24) break;
                Game game = createGame(distance, date, i, totalPrize);
                gameRepository.save(game);
            }
        }
    }

    private Game createGame(GameDistance distance, LocalDate date, int startTime, double totalPrize){
        return Game.builder()
                .name(GameType.CADENCE.getName() + "-" + distance.getName())
                .type(GameType.CADENCE)
                .distance(distance)
                .gameDate(date)
                .startAt(date.atTime(startTime, 0)) // 예: 2025-07-21T05:00
                .endAt(date.atTime(startTime, 0).plusHours(distance.getTime())) // 예: +3시간 등
                .participatedCount(0)
                .totalPrize(totalPrize)
                .firstPlacePrize(calculateFirstPlacePrize(totalPrize))
                .secondPlacePrize(calculateSecondPlacePrize(totalPrize))
                .thirdPlacePrize(calculateThirdPlacePrize(totalPrize))
                .build();
    }
}
