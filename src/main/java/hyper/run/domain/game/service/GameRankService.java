package hyper.run.domain.game.service;

import hyper.run.domain.game.dto.response.RankResponse;

import java.time.LocalDate;

public interface GameRankService {
    RankResponse calculateRank(Long gameId);

    void generateGame(LocalDate date, double totalPrize);
}
