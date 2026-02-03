package hyper.run.game;

import hyper.run.domain.game.dto.request.GameHistoryBatchUpdateRequest;
import hyper.run.domain.game.dto.request.GameHistoryUpdateRequest;
import hyper.run.domain.game.dto.response.GameHistoryBatchUpdateResponse;
import hyper.run.domain.game.dto.response.GameHistoryResponse;
import hyper.run.domain.game.dto.response.GameInProgressWatchResponse;
import hyper.run.domain.game.service.GameHistoryCacheService;
import hyper.run.domain.game.service.GameHistoryService;
import hyper.run.domain.game.service.GameService;
import hyper.run.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/game-histories")
public class GameHistoryController {

    private final GameService gameService;
    private final GameHistoryCacheService gameHistoryCacheService;
    private final GameHistoryService gameHistoryService;

    /**
     * 자신의 생체 데이터를 업데이트 하는 API
     */
    @PatchMapping
    public ResponseEntity<?> updateGameHistory(@RequestBody final GameHistoryUpdateRequest gameHistoryUpdateRequest){
        gameHistoryService.updateAsyncGameHistory(gameHistoryUpdateRequest);
        SuccessResponse response = new SuccessResponse(true, "내 생체 데이터 정보 업데이트", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Apple Watch용 HTTP Polling 최적화 - 배치 업데이트 API
     * 클라이언트에서 여러 생체 데이터 샘플을 모아서 한 번에 전송
     * 응답에 동적 폴링 주기를 포함하여 트래픽 최적화
     *
     * 폴링 주기:
     * - 경기 시작 직후 (10% 이하): 5초
     * - 일반 진행 중: 10초
     * - 완주 임박 (90% 이상): 3초
     * - 완주 후: -1 (폴링 중단)
     */
    @PatchMapping("/batch")
    public ResponseEntity<?> updateGameHistoryBatch(@RequestBody final GameHistoryBatchUpdateRequest request) {
        gameHistoryService.updateBatchGameHistory(request);
        SuccessResponse response = new SuccessResponse(true, "배치 업데이트 완료", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 해당 경기의 1위(rank=1) 경기 기록을 조회하는 API
     */
    @GetMapping("/first-place-history")
    public ResponseEntity<?> getFirstPlaceGameHistory(@RequestParam Long gameId){
        GameHistoryResponse firstPlaceHistory = gameService.findFirstPlaceGameHistory(gameId);
        SuccessResponse response = new SuccessResponse(true, "1위 경기 기록 조회 성공", firstPlaceHistory);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 해당 경기의 1위(rank=1) 경기 기록을 조회하는 API (POLLING 용)
     */
    @GetMapping("/first-status")
    public ResponseEntity<?> getFirstStatus(@RequestParam Long gameId){
        GameInProgressWatchResponse firstPlaceHistory = gameHistoryCacheService.getFirstPlaceStatus(gameId);
        SuccessResponse response = new SuccessResponse(true, "1위 경기 상태 조회 성공", firstPlaceHistory);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/status")
    public ResponseEntity<?> getGameCurrentStatus(@RequestParam Long gameId, @RequestParam Long userId){
        GameInProgressWatchResponse game = gameHistoryCacheService.getUserStatus(gameId, userId);
        SuccessResponse response = new SuccessResponse(true, "현재 내 등수 조회 성공", game);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
