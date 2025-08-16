package hyper.run.domain.game.service;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameType;

import java.time.LocalDate;

public interface GameRankService {
    void calculateRank(Game game);

    void generateGame(LocalDate date);

    void saveGameResult(Game game);

    GameType getGameType();
}
