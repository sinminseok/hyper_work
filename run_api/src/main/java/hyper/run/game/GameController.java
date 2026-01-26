package hyper.run.game;

import hyper.run.domain.game.dto.request.GameApplyRequest;
import hyper.run.domain.game.dto.request.GamePrizeCursorRequest;
import hyper.run.domain.game.dto.response.GameCalendarResponse;
import hyper.run.domain.game.dto.response.GameHistoryResponse;
import hyper.run.domain.game.dto.response.GameInProgressWatchResponse;
import hyper.run.domain.game.dto.response.GameResponse;
import hyper.run.domain.game.dto.response.WeeklyExerciseResponse;
import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.game.service.GameRankService;
import hyper.run.domain.game.service.GameService;
import hyper.run.domain.user.dto.request.EmailRequest;
import hyper.run.utils.EmailService;
import hyper.run.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static hyper.run.auth.service.SecurityContextHelper.getLoginEmailBySecurityContext;
import static hyper.run.auth.service.SecurityContextHelper.getLoginUserIdBySecurityContext;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/games")
public class GameController {

    private final GameService gameService;
    private final Map<GameType, GameRankService> gameRankServices;
    private final EmailService emailService;
    private final GameScheduler gameScheduler;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest request) {
        try {
            emailService.sendSimpleEmail(
                    request.getTo(),
                    request.getSubject(),
                    request.getText()
            );
            return ResponseEntity.ok("이메일 발송 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("이메일 발송 실패: " + e.getMessage());
        }
    }

//    //todo 삭제
    @PostMapping("/test/create")
    public ResponseEntity<?> testStart(){
        gameService.test();
        SuccessResponse response = new SuccessResponse(true, "경기 예약 성공", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //todo 삭제
    @PostMapping("/test/create-game")
    public ResponseEntity<?> testStart2(){
        for(GameType type : GameType.values()) {
            LocalDate oneWeekLater = LocalDate.now().plusWeeks(1);
            gameRankServices.get(type).generateGame(oneWeekLater);
        }
        SuccessResponse response = new SuccessResponse(true, "경기 예약 성공", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //todo 삭제
    @PostMapping("/test/start/{gameId}")
    public ResponseEntity<?> testStartGame(@PathVariable Long gameId){
        gameScheduler.testStart(gameId);
        SuccessResponse response = new SuccessResponse(true, "경기 강제 시작 성공", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 상금 상위 3개를 조회하는 API
     */
    @GetMapping("/top-prize")
    public ResponseEntity<?> getTopPrizeGames() {
        Long userId = getLoginUserIdBySecurityContext();
        List<GameResponse> topRankGames = gameService.findTopRankGames(userId);
        SuccessResponse response = new SuccessResponse(true, "상위 3개 게임 조회 성공", topRankGames);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 상금 순으로 정렬된 게임을 커서 기반으로 조회하는 API (상위 4개부터 조회)
     */
    @GetMapping("/prize-list")
    public ResponseEntity<?> getGamesByPrize(@RequestParam(required = false) Double cursorTotalPrize,
                                              @RequestParam(required = false) Long cursorGameId,
                                              @RequestParam(defaultValue = "10") Integer size) {
        Long userId = getLoginUserIdBySecurityContext();
        GamePrizeCursorRequest request = GamePrizeCursorRequest.of(cursorTotalPrize, cursorGameId, size);

        List<GameResponse> games = gameService.findGamesOrderByPrize(userId, request);
        SuccessResponse response = new SuccessResponse(true, "상금 순 게임 목록 조회 성공", games);
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
        Long userId = getLoginUserIdBySecurityContext();
        gameService.applyGame(userId, request);
        SuccessResponse response = new SuccessResponse(true, "경기 예약 성공", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 예정된 경기 취소 API
     */
    @PatchMapping("/{gameId}/cancel")
    public ResponseEntity<?> cancelGame(@PathVariable Long gameId) {
        Long userId = getLoginUserIdBySecurityContext();
        gameService.cancelGame(userId, gameId);
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
     * gameId로 나의 GameHistory를 단건 조회하는 API
     */
    @GetMapping("/my-history")
    public ResponseEntity<?> getMyGameHistory(@RequestParam Long gameId){
        Long userId = getLoginUserIdBySecurityContext();
        GameHistoryResponse gameHistoryResponse = gameService.findGameHistoryByGameIdAndUserId(gameId, userId);
        SuccessResponse response = new SuccessResponse(true, "나의 경기 기록 조회 성공", gameHistoryResponse);
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
     * 내가 참가한 모든 경기 조회 API
     */
    @GetMapping("/participated")
    public ResponseEntity<?> getMyParticipatedGames(){
        String email = getLoginEmailBySecurityContext();
        List<GameResponse> participatedGames = gameService.findMyParticipatedGames(email);
        SuccessResponse response = new SuccessResponse(true, "참가한 모든 경기 조회 성공", participatedGames);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 이번주 운동 기록을 조회하는 API
     */
    @GetMapping("/weekly-exercise")
    public ResponseEntity<?> getWeeklyExerciseRecord(){
        String email = getLoginEmailBySecurityContext();
        WeeklyExerciseResponse weeklyExercise = gameService.findWeeklyExerciseRecord(email);
        SuccessResponse response = new SuccessResponse(true, "이번주 운동 기록 조회 성공", weeklyExercise);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 특정 년/월의 경기 기록을 조회하는 API
     */
    @GetMapping("/calendar")
    public ResponseEntity<?> getMonthlyGameRecords(@RequestParam int year, @RequestParam int month){
        String email = getLoginEmailBySecurityContext();
        List<GameCalendarResponse> monthlyRecords = gameService.findMonthlyGameRecords(email, year, month);
        SuccessResponse response = new SuccessResponse(true, "월별 경기 기록 조회 성공", monthlyRecords);
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
}
