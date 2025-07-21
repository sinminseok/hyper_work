package hyper.run.domain.game.service;

import hyper.run.domain.game.dto.response.RankResponse;
import hyper.run.domain.game.entity.Game;

import java.time.LocalDate;

public interface GameRankService {
    void calculateRank(Game game);

    void generateGame(LocalDate date, double totalPrize);
}
