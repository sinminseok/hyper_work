package hyper.run.game;

import hyper.run.domain.game.dto.request.GameApplyRequest;
import hyper.run.domain.game.dto.response.GameHistoryResponse;
import hyper.run.domain.game.dto.response.GameInProgressWatchResponse;
import hyper.run.domain.game.dto.response.GameResponse;
import hyper.run.domain.game.service.GameService;
import hyper.run.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static hyper.run.auth.service.SecurityContextHelper.getLoginEmailBySecurityContext;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/games")
public class GameController {

    private final GameService gameService;

    //todo 삭제
    @PostMapping("/test/start")
    public ResponseEntity<?> testStart(@RequestParam Long gameId){
        gameService.testStart(gameId);
        SuccessResponse response = new SuccessResponse(true, "경기 예약 성공", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 진행중인 경기에서 1등 정보를 조회하는 API
     */
    @GetMapping("/history/first-place")
    public ResponseEntity<?> getFirstPlace(@RequestParam Long gameId){
        GameInProgressWatchResponse gameInProgressWatchResponse = gameService.findFirstPlaceByGameId(gameId);
        SuccessResponse response = new SuccessResponse(true, "1등 정보 조회 성공", gameInProgressWatchResponse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 경기 신청 API
     */
    @PostMapping("/apply")
    public ResponseEntity<?> applyGame(@RequestBody GameApplyRequest request){
        String email = getLoginEmailBySecurityContext();
        gameService.applyGame(email, request);
        SuccessResponse response = new SuccessResponse(true, "경기 예약 성공", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 예정된 경기 취소 API
     */
    @PatchMapping("/{gameId}/cancel")
    public ResponseEntity<?> cancelGame(@PathVariable Long gameId) {
        String email = getLoginEmailBySecurityContext();
        gameService.cancelGame(email, gameId);
        SuccessResponse response = new SuccessResponse(true, "경기 참여 철회", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 참가중인 경기 포기 API
     */
    @PatchMapping("/{gameId}/give-up")
    public ResponseEntity<?> giveUpGame(@PathVariable Long gameId) {
        String email = getLoginEmailBySecurityContext();
        gameService.giveUpGame(email, gameId);
        SuccessResponse response = new SuccessResponse(true, "경기 포기", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 메인 페이지에서 조회할 게임 API
     */
    @GetMapping
    public ResponseEntity<?> getMainGamesAll(){
        String email = getLoginEmailBySecurityContext();
        List<GameResponse> myParticipateGames = gameService.findGames(email);
        SuccessResponse response = new SuccessResponse(true, "참가중인 경기 조회 성공", myParticipateGames);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 자신의 경기 참여 내역 조회 API (순위, 상금 포함)
     */
    @GetMapping("/history")
    public ResponseEntity<?> getGameHistories(){
        String email = getLoginEmailBySecurityContext();
        List<GameHistoryResponse> gameHistories = gameService.findMyGameHistories(email);
        SuccessResponse response = new SuccessResponse(true, "경기 기록 조회 성공", gameHistories);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 경기 기록을 단일 조회한다.
     */
    @GetMapping("/history/{gameId}")
    public ResponseEntity<?> getGameHistory(@PathVariable Long gameId){
        String email = getLoginEmailBySecurityContext();
        GameHistoryResponse gameHistoryResponse = gameService.findGameHistory(email, gameId);
        SuccessResponse response = new SuccessResponse(true, "경기 기록 단일 조회 성공", gameHistoryResponse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 진행중 Or 참가 예정인 자신의 경기 모두 조회 API
     */
    @GetMapping("/participate/history")
    public ResponseEntity<?> getParticipateGameHistories(){
        String email = getLoginEmailBySecurityContext();
        List<GameHistoryResponse> gameHistories = gameService.findMyParticipateGames(email);
        SuccessResponse response = new SuccessResponse(true, "참가중 or 참가 예정인 경기 기록 조회", gameHistories);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * 경기 단일 조회 API
     */
    @GetMapping("/{gameId}")
    public ResponseEntity<?> getGame(@PathVariable Long gameId){
        GameResponse gameResponse = gameService.findById(gameId);
        SuccessResponse response = new SuccessResponse(true, "경기 단일 조회 성공", gameResponse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 자신의 경기 미션을 도달했음을 알리는 API
     */
    @PostMapping("/quit")
    public ResponseEntity<?> quitGame(){
        SuccessResponse response = new SuccessResponse(true, "내 경기 종료", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
