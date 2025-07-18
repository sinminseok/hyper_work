package hyper.run.domain.game.controller;

import hyper.run.domain.game.service.GameService;
import hyper.run.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static hyper.run.auth.service.SecurityContextHelper.getLoginEmailBySecurityContext;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/games")
public class GameController {

    private final GameService gameService;

    /**
     * 예정 경기는 모두 반환
     *
     * 참가중인 경기 (경기중, 경기 예정-참가 신청완료)
     */
//    @GetMapping
//    public ResponseEntity<?> findAll() {
//        gameService.fin
//    }

    /**
     * 경기 신청 API
     */
    @PostMapping
    public ResponseEntity<?> applyGame(@RequestParam Long gameId){
        String email = getLoginEmailBySecurityContext();
        gameService.applyGame(email, gameId);
        SuccessResponse response = new SuccessResponse(true, "경기 예약 성공", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
