package hyper.run.domain.game.service;

import hyper.run.domain.game.dto.request.GameHistoryUpdateRequest;
import hyper.run.domain.game.dto.response.GameInProgressWatchResponse;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class GameHistoryService {

    private final GameHistoryRepository repository;

    @Transactional
    public GameInProgressWatchResponse updateGameHistory(final GameHistoryUpdateRequest request){
        GameHistory gameHistory = OptionalUtil.getOrElseThrow(repository.findByUserIdAndGameId(request.getUserId(), request.getGameId()), "게임 history 가 존재하지 않습니다.");
        gameHistory.updateCurrentValue(request);
        return GameInProgressWatchResponse.toResponse(gameHistory);
    }
}
