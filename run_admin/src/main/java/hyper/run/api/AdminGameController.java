package hyper.run.api;

import hyper.run.domain.game.dto.response.admin.AdminGameResponse;
import hyper.run.domain.game.entity.AdminGameStatus;
import hyper.run.domain.game.entity.GameStatus;
import hyper.run.domain.game.service.GameService;
import hyper.run.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/admin/games")
public class AdminGameController {

    private final GameService gameService;

    @GetMapping
    public ResponseEntity<?> getAllGames(
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                          @RequestParam(defaultValue = "FINISHED") AdminGameStatus adminGameStatus,
                                          @RequestParam(required = false) String keyword,
                                          @PageableDefault(size = 6) Pageable pageable) {
        Page<AdminGameResponse> gamePage = gameService.findAllGames(startDate,endDate,adminGameStatus,keyword,pageable);
        SuccessResponse response = new SuccessResponse(true,"게임 조회 성공", gamePage);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
