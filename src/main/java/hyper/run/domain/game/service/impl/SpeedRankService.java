package hyper.run.domain.game.service.impl;

import hyper.run.domain.game.dto.response.RankResponse;
import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.domain.game.service.GameRankService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 가장 멀리 달리고 있는 사람순서대로 순위 정하기
 */
@Service
@RequiredArgsConstructor
public class SpeedRankService implements GameRankService {

    private final GameRepository gameRepository;
    private final GameHistoryRepository gameHistoryRepository;


    /**
     * 스피드가 빠를수록 높은 점수 (이동한 거리가 많을수록 높은 점수)
     */
    @Override
    public void calculateRank(Game game) {
        List<GameHistory> gameHistories = gameHistoryRepository.findAllByGameId(game.getId());

        // 1. currentDistance 기준으로 내림차순 정렬
        gameHistories.sort((g1, g2) -> Double.compare(g2.getCurrentDistance(), g1.getCurrentDistance()));

        // 2. 순위 매기기 (1등부터 시작)
        int rank = 1;
        for (GameHistory history : gameHistories) {
            history.setRank(rank++);
        }

        // 3. 업데이트된 rank를 DB에 반영
        gameHistoryRepository.saveAll(gameHistories);
    }

    @Override
    public void generateGame(LocalDate date, double totalPrize) {

    }
}
