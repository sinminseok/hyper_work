package hyper.run.domain.game.controller;

import hyper.run.domain.game.dto.request.GameApplyRequest;
import hyper.run.domain.game.dto.response.GameResponse;
import hyper.run.domain.game.service.GameService;
import hyper.run.domain.game.service.impl.CadenceRankService;
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
    private final CadenceRankService cadenceRankService;

    /**
     * 경기 생성 테스트용 API
     */
    @PostMapping("/test")
    public ResponseEntity<?> testGameGenerate(){
        cadenceRankService.generateGame(LocalDate.of(2025,10,23), 100000);
        SuccessResponse response = new SuccessResponse(true, "경기 예약 성공", null);
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

    @PatchMapping("/{gameId}/cancel")
    public ResponseEntity<?> cancelGame(@PathVariable Long gameId) {
        String email = getLoginEmailBySecurityContext();
        gameService.cancelGame(email, gameId);
        SuccessResponse response = new SuccessResponse(true, "경기 참여 취소", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 메인 페이지에서 조회할 게임 API
     */
    @GetMapping
    public ResponseEntity<?> getMainGamesAll(){
        String email = getLoginEmailBySecurityContext();
        List<GameResponse> myParticipateGames = gameService.getGames(email);
        SuccessResponse response = new SuccessResponse(true, "참가중인 경기 조회 성공", myParticipateGames);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
