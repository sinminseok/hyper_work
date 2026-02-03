package hyper.run.domain.game.service;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.entity.GameType;

import java.time.LocalDate;
import java.util.List;

public interface GameRankService {
    void calculateRank(Game game);

    void generateGame(LocalDate date);

    void saveGameResult(Game game);

    GameType getGameType();

    /**
     * 주어진 히스토리 목록에서 1위를 찾아 반환
     */
    GameHistory findFirstPlace(List<GameHistory> histories);
}
