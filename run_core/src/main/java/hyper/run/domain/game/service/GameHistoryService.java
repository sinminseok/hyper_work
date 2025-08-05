package hyper.run.domain.game.service;

import hyper.run.domain.game.dto.request.GameHistoryUpdateRequest;
import hyper.run.domain.game.dto.response.GameInProgressWatchResponse;
import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GameHistoryService {

    private final Map<GameType, GameRankService> gameRankServices;
    private final GameHistoryRepository repository;
    private final GameRepository gameRepository;

    @Transactional
    public GameInProgressWatchResponse updateGameHistory(final GameHistoryUpdateRequest request){
        GameHistory gameHistory = OptionalUtil.getOrElseThrow(repository.findByUserIdAndGameId(request.getUserId(), request.getGameId()), "게임 history 가 존재하지 않습니다.");
        gameHistory.updateCurrentValue(request);
        gameHistory.checkDoneGameByDistance();
        repository.save(gameHistory);
        return GameInProgressWatchResponse.toResponse(gameHistory);
    }

    @Transactional
    public void quitGame(final GameHistoryUpdateRequest request){
        GameHistory gameHistory = OptionalUtil.getOrElseThrow(repository.findByUserIdAndGameId(request.getUserId(), request.getGameId()), "게임 history 가 존재하지 않습니다.");
        Game game = OptionalUtil.getOrElseThrow(gameRepository.findById(gameHistory.getGameId()), "존재하지 않는 게임 ID 입니다.");
        GameRankService gameRankService = gameRankServices.get(game.getType());
        gameRankService.calculateRank(game); //경기 순위 최신화
        gameHistory.setDone(true);
        repository.save(gameHistory);
    }
}
