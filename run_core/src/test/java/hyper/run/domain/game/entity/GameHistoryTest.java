package hyper.run.domain.game.entity;

import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.game.repository.GameRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static hyper.run.helper.GameHelper.*;

@SpringBootTest
public class GameHistoryTest {

    @Autowired
    private GameHistoryRepository gameHistoryRepository;

    @Test
    void 케이던스_계산_테스트(){
        //given
        GameHistory gameHistory = gameHistoryRepository.save(generateCadenceGameHistory("1", 1L, 1L, 150, false));

        //when
        gameHistory.updateCurrentValue(generateCadenceGameHistoryUpdateRequest(1L, 1L, 150));
        gameHistory.updateCurrentValue(generateCadenceGameHistoryUpdateRequest(1L, 1L, 300));
        gameHistory.updateCurrentValue(generateCadenceGameHistoryUpdateRequest(1L, 1L, 900));

        Assertions.assertThat(gameHistory.getCurrentCadence()).isEqualTo(450.0);
    }


}
