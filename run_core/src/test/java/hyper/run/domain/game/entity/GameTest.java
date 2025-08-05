package hyper.run.domain.game.entity;

import hyper.run.domain.game.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static hyper.run.helper.GameHelper.generateGame;

@SpringBootTest
public class GameTest {

    @Autowired
    private GameRepository gameRepository;


}
