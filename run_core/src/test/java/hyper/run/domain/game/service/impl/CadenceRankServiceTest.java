package hyper.run.domain.game.service.impl;


import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameDistance;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.domain.game.service.GameHistoryService;
import hyper.run.domain.game.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static hyper.run.helper.GameHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CadenceRankServiceTest {

    @Autowired
    private CadenceRankService cadenceRankService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameHistoryRepository gameHistoryRepository;

    @Autowired
    private GameService gameService;

    @Autowired
    private GameHistoryService gameHistoryService;

    @BeforeEach
    void setInit(){
        gameHistoryRepository.deleteAll();
    }

    @Test
    void 케이던스_순위_계산_알고리즘_테스트() {
        //given
        Game game = gameRepository.save(generateGame(GameType.CADENCE, GameDistance.FIVE_KM_COURSE));
        for (int i = 0; i < 3; i++) {
            gameHistoryRepository.save(generateCadenceGameHistory(String.valueOf(i + 1), game.getId(), (long) i, 120, false));
        }

        //when
        gameHistoryService.updateGameHistory(generateCadenceGameHistoryUpdateRequest(game.getId(), 0L, 120)); // 1등
        gameHistoryService.updateGameHistory(generateCadenceGameHistoryUpdateRequest(game.getId(), 1L, 130)); // 2등
        gameHistoryService.updateGameHistory(generateCadenceGameHistoryUpdateRequest(game.getId(), 2L, 150)); // 3등
        cadenceRankService.calculateRank(game);

        //then
        List<GameHistory> allByGameId = gameHistoryRepository.findAllByGameId(game.getId());

        // 검증: userId 0L → rank 1, 1L → 2, 2L → 3
        Map<Long, Integer> expected = Map.of(0L, 1, 1L, 2, 2L, 3);

        for (GameHistory history : allByGameId) {
            Long userId = history.getUserId();
            int expectedRank = expected.get(userId);
            assertThat(history.getRank())
                    .as("userId %d 의 예상 등수는 %d", userId, expectedRank)
                    .isEqualTo(expectedRank);
        }
    }

    @Test
    void 이미_완료된_사용자가_있을경우_순위_계산_테스트(){
        //given
        Game game = gameRepository.save(generateGame(GameType.CADENCE, GameDistance.FIVE_KM_COURSE));
        for(int i=0; i < 5; i++){
            gameHistoryRepository.save(generateCadenceGameHistory(String.valueOf(i+1), game.getId(), (long) i, 120, false));
        }
        for(int i=5; i < 10; i++){
            gameHistoryRepository.save(generateCadenceGameHistory(String.valueOf(i+1), game.getId(), (long) i, 120, true));
        }

        //when
        cadenceRankService.calculateRank(game);
        Optional<GameHistory> gameHistory = gameHistoryRepository.findById("1");

        //then
        assertThat(gameHistory.get().getRank()).isGreaterThan(5);
    }

}
