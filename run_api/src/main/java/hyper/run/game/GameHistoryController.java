package hyper.run.game;

import hyper.run.domain.game.dto.request.GameHistoryUpdateRequest;
import hyper.run.domain.game.service.GameHistoryService;
import hyper.run.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/game-histories")
public class GameHistoryController {

    private final GameHistoryService gameHistoryService;

    /**
     * 웹소켓에 연결되어 있지 않을때 생체 데이터 업데이트
     */
    @PatchMapping
    public ResponseEntity<?> updateGameHistory(@RequestBody final GameHistoryUpdateRequest gameHistoryUpdateRequest){
        gameHistoryService.updateAsyncGameHistory(gameHistoryUpdateRequest);
        SuccessResponse response = new SuccessResponse(true, "내 생체 데이터 정보 업데이트", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
