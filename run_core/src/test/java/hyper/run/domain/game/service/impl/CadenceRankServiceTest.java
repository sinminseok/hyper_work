package hyper.run.domain.game.service.impl;


import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameDistance;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.domain.game.service.GameHistoryService;
import hyper.run.domain.game.service.GameService;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static hyper.run.helper.GameHelper.*;
import static hyper.run.helper.UserHelper.generateUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setInit(){
        gameHistoryRepository.deleteAll();
    }

    @Test
    void 케이던스_순위_계산_알고리즘_테스트() {
        //given
        Game game = gameRepository.save(generateGame(GameType.CADENCE, GameDistance.FIVE_KM_COURSE));
        for (int i = 0; i < 3; i++) {
            gameHistoryRepository.save(generateCadenceGameHistory(String.valueOf(i + 1), game.getId(), (long) i, 120, false, 0));
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
            gameHistoryRepository.save(generateCadenceGameHistory(String.valueOf(i+1), game.getId(), (long) i, 120, false, 0));
        }
        for(int i=5; i < 10; i++){
            gameHistoryRepository.save(generateCadenceGameHistory(String.valueOf(i+1), game.getId(), (long) i, 120, true, i - 4));
        }

        //when
        cadenceRankService.calculateRank(game);
        Optional<GameHistory> gameHistory = gameHistoryRepository.findById("1");

        //then
        assertThat(gameHistory.get().getRank()).isGreaterThan(5);
    }

    @Test
    void 게임_결과_저장_테스트(){
        Game game = gameRepository.save(generateGame(GameType.CADENCE, GameDistance.FIVE_KM_COURSE));
        List<Long> userIds = new ArrayList<>();
        for(int i=0; i < 3; i++){//아직 게임 진행중인 사람들
            User user = userRepository.save(generateUser((long)i));
            userIds.add(user.getId());
            gameHistoryRepository.save(generateCadenceGameHistory(String.valueOf(i+1), game.getId(), user.getId(), 120, false, 0));
        }
        for(int i=3; i < 6; i++){ //3명의 경기 완료된 사람들
            User user = userRepository.save(generateUser((long)i));
            gameHistoryRepository.save(generateCadenceGameHistory(String.valueOf(i+1), game.getId(), user.getId(), 120, true, i - 2));
        }

        //when
        gameHistoryService.updateGameHistory(generateCadenceGameHistoryUpdateRequest(game.getId(), userIds.get(0), 120)); // 1등
        gameHistoryService.updateGameHistory(generateCadenceGameHistoryUpdateRequest(game.getId(), userIds.get(1), 130)); // 2등
        gameHistoryService.updateGameHistory(generateCadenceGameHistoryUpdateRequest(game.getId(), userIds.get(2), 150)); // 3등
        cadenceRankService.calculateRank(game);

        cadenceRankService.saveGameResult(game);

        List<GameHistory> allByGameId = gameHistoryRepository.findAllByGameId(game.getId());


        for (GameHistory gameHistory : allByGameId) {
            Long gameId = Long.valueOf(gameHistory.getId());
            int rank = gameHistory.getRank();
            // 검증 조건: gameId와 rank의 관계
            boolean isValid =
                    (gameId >= 4 && gameId <= 6 && rank >= 1 && rank <= 3) ||
                            (gameId >= 1 && gameId <= 3 && rank >= 4 && rank <= 6);
            assertThat(gameHistory.isDone()).isTrue();
            assertTrue(isValid, "조건 불일치 - gameId: " + gameId + ", rank: " + rank);
        }
    }
}
