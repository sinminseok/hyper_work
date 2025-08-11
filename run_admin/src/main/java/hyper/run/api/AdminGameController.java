package hyper.run.api;

import hyper.run.domain.game.dto.response.AdminGameResponse;
import hyper.run.domain.game.entity.GameStatus;
import hyper.run.domain.game.service.GameService;
import hyper.run.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/admin/games")
public class AdminGameController {

    private final GameService gameService;

    @GetMapping
    public ResponseEntity<?> getAllGames( // 생성일자(createdAt) 필터링 시작 날짜
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                          // 생성일자(createdAt) 필터링 종료 날짜
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                          // 게임 상태(탭) 필터링
                                          @RequestParam(defaultValue = "FINISHED") String status,
                                          // 키워드 검색 (추후 확장 가능)
                                          @RequestParam(required = false) String keyword,
                                          @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        GameStatus statusEnum = GameStatus.valueOf(status.toUpperCase());
        Page<AdminGameResponse> gamePage = gameService.findAllGames(startDate,endDate,statusEnum,keyword,pageable);
        SuccessResponse response = new SuccessResponse(true,"게임 조회 성공", gamePage);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
