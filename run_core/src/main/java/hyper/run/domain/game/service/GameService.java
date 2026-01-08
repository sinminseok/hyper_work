package hyper.run.domain.game.service;

import hyper.run.domain.game.dto.request.GameApplyRequest;
import hyper.run.domain.game.dto.request.GamePrizeCursorRequest;
import hyper.run.domain.game.dto.response.GameCalendarResponse;
import hyper.run.domain.game.dto.response.GameHistoryResponse;
import hyper.run.domain.game.dto.response.GameInProgressWatchResponse;
import hyper.run.domain.game.dto.response.GameResponse;
import hyper.run.domain.game.dto.response.WeeklyExerciseResponse;
import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.entity.GameStatus;
import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.domain.game.repository.GameRepositoryCustom;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.exception.custom.AlreadyApplyGameException;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static hyper.run.exception.ErrorMessages.*;

@Service
@RequiredArgsConstructor
public class GameService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final GameRepositoryCustom gameRepositoryCustom;
    private final Map<GameType, GameRankService> gameRankServices;

    @Transactional
    public void test() {
        for(GameType type : GameType.values()) {
            LocalDate oneWeekLater = LocalDate.now().plusWeeks(1);
            gameRankServices.get(type).generateGame(oneWeekLater);
        }
    }

    /**
     * 게임 참가 신청 메서드
     */
    @Transactional
    public void applyGame(final Long userId, final GameApplyRequest request) {
        Game game = OptionalUtil.getOrElseThrow(
                gameRepositoryCustom.findByGameConditions(
                        request.getStartAt(),
                        request.getDistance(),
                        request.getType(),
                        request.getActivityType()), NOT_EXIST_GAME_ID);
        if(gameHistoryRepository.findByUserIdAndGameId(userId, game.getId()) != null) {
            throw new AlreadyApplyGameException("이미 신청한 경기 입니다.");
        }
        game.applyGame(userId, request.getAverageBpm(), request.getTargetCadence());
        gameRepository.save(game);
    }

    /**
     * 참가 철회 메서드
     */
    @Transactional
    public void cancelGame(final Long userId, final Long gameId){
        Game game = OptionalUtil.getOrElseThrow(gameRepository.findByIdForUpdate(gameId), NOT_EXIST_GAME_ID);
        game.cancelGame(userId);
        gameRepository.save(game);
    }

    /**
     * 참가 취소 메서드
     */
    @Transactional
    public void giveUpGame(final String email, final Long gameId){
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        GameHistory gameHistory = OptionalUtil.getOrElseThrow(gameHistoryRepository.findByUserIdAndGameId(user.getId(), gameId), NOT_EXIST_GAME_GISTORY_ID);
        gameHistoryRepository.delete(gameHistory);
    }

    /**
     * 예정된 경기를 조회한다. 이때 자신이 이미 신청한 경기와 신청하지 않은 경기를 구분한다.
     */
    public List<GameResponse> findGames(final String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new IllegalArgumentException(NOT_EXIST_USER_EMAIL));
        Set<Long> participatedGameIds = gameHistoryRepository.findAllByUserId(user.getId()).stream()
                .map(GameHistory::getGameId)
                .collect(Collectors.toSet());
        LocalDateTime now = LocalDateTime.now();
        return gameRepository.findUpcomingGames(now).stream()
                .filter(game -> game.isInProgress() || game.isNotYetStart())
                .map(game -> GameResponse.toResponse(game, determineGameStatus(game, participatedGameIds)))
                .collect(Collectors.toList());
    }

    /**
     * 상금 상위 3개 경기를 조회하는 메서드
     */
    public List<GameResponse> findTopRankGames(Long userId){
        Set<Long> participatedGameIds = gameHistoryRepository.findAllByUserId(userId).stream()
                .map(GameHistory::getGameId)
                .collect(Collectors.toSet());
        LocalDateTime now = LocalDateTime.now();
        return gameRepository.findTop3UpcomingGamesByTotalPrize(now).stream()
                .map(game -> GameResponse.toResponse(game, determineGameStatus(game, participatedGameIds)))
                .collect(Collectors.toList());
    }

    /**
     * 상금 기준으로 정렬된 경기를 커서 기반으로 조회하는 메서드 (상위 4개부터 조회)
     */
    public List<GameResponse> findGamesOrderByPrize(Long userId, GamePrizeCursorRequest request) {
        Set<Long> participatedGameIds = gameHistoryRepository.findAllByUserId(userId).stream()
                .map(GameHistory::getGameId)
                .collect(Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();
        List<Game> games;

        if (request.hasCursor()) {
            games = gameRepositoryCustom.findGamesOrderByPrizeWithCursor(
                    now,
                    request.getCursorTotalPrize(),
                    request.getCursorGameId(),
                    request.getSize()
            );
        } else {
            games = gameRepositoryCustom.findGamesOrderByPrizeWithoutCursor(now, request.getSize());
        }

        return games.stream()
                .map(game -> GameResponse.toResponse(game, determineGameStatus(game, participatedGameIds)))
                .collect(Collectors.toList());
    }

    /**
     * 자신의 종료된 경기 참여 내역을 모두 조회한다.
     */
    public List<GameHistoryResponse> findMyGameHistories(final String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(NOT_EXIST_USER_EMAIL));
        return gameHistoryRepository.findAllByUserId(user.getId()).stream()
                .filter(gameHistory -> gameHistory.isDone())
                .map(gameHistory -> {
                    Game game = OptionalUtil.getOrElseThrow(gameRepository.findById(gameHistory.getGameId()), NOT_EXIST_GAME_ID);
                    return GameHistoryResponse.toResponse(game, gameHistory);
                })
                .toList();
    }

    /**
     * 내가 참가한 모든 경기를 조회하는 메서드
     */
    public List<GameResponse> findMyParticipatedGames(final String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(NOT_EXIST_USER_EMAIL));
        return gameHistoryRepository.findAllByUserId(user.getId()).stream()
                .map(gameHistory -> {
                    Game game = OptionalUtil.getOrElseThrow(gameRepository.findById(gameHistory.getGameId()), NOT_EXIST_GAME_ID);
                    return GameResponse.toResponse(game, determineParticipatedGameStatus(game));
                })
                .collect(Collectors.toList());
    }

    private GameStatus determineParticipatedGameStatus(Game game) {
        if (game.isInProgress()) {
            return GameStatus.IN_PROGRESS;
        }
        if (game.isNotYetStart()) {
            return GameStatus.REGISTRATION_COMPLETE;
        }
        return GameStatus.PARTICIPATE_FINISH;
    }

    /**
     * 현재 참가중인 경기 기록 단일 조회한다.
     */
    public GameHistoryResponse findGameHistory(final String email, final  Long gameId){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(NOT_EXIST_USER_EMAIL));
        Game game = OptionalUtil.getOrElseThrow(gameRepository.findById(gameId), NOT_EXIST_GAME_ID);
        GameHistory gameHistory = OptionalUtil.getOrElseThrow(gameHistoryRepository.findByUserIdAndGameId(user.getId(), gameId), NOT_EXIST_GAME_GISTORY_ID);
        return GameHistoryResponse.toResponse(game, gameHistory);
    }

    /**
     * 자신의 참가중 or 참가 예정인 경기 내역을 모두 조회한다.
     */
    public List<GameHistoryResponse> findMyParticipateGames(final String email){
        System.out.println("emailemail===" + email);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(NOT_EXIST_USER_EMAIL));
        return gameHistoryRepository.findAllByUserId(user.getId()).stream()
                .map(gameHistory -> {
                    System.out.println("gameHistory = " + gameHistory.getGameId());
                    Game game = OptionalUtil.getOrElseThrow(gameRepository.findById(gameHistory.getGameId()), NOT_EXIST_GAME_ID);
                    if (game.isInProgress() || game.isNotYetStart()) {
                        return GameHistoryResponse.toResponse(game, gameHistory);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 경기 단일 조회 by gameID
     */
    public GameResponse findById(final Long gameId){
        Game game = OptionalUtil.getOrElseThrow(gameRepository.findById(gameId), NOT_EXIST_GAME_ID);
        return GameResponse.toResponse(game, GameStatus.PARTICIPATE_FINISH);
    }

    /**
     * 현재 경기 상태 조회 메서드
     */
    public GameInProgressWatchResponse getCurrentGameStatus(Long gameId, Long userId) {
        GameHistory gameHistory = OptionalUtil.getOrElseThrow(gameHistoryRepository.findByUserIdAndGameId(userId, gameId), NOT_EXIST_GAME_ID);
        return GameInProgressWatchResponse.toResponse(gameHistory);
    }

    /**
     * gameId와 userId로 GameHistory를 단건 조회하는 메서드
     */
    public GameHistoryResponse findGameHistoryByGameIdAndUserId(Long gameId, Long userId) {
        GameHistory gameHistory = OptionalUtil.getOrElseThrow(
                gameHistoryRepository.findByUserIdAndGameId(userId, gameId),
                NOT_EXIST_GAME_GISTORY_ID
        );
        Game game = OptionalUtil.getOrElseThrow(gameRepository.findById(gameId), NOT_EXIST_GAME_ID);
        return GameHistoryResponse.toResponse(game, gameHistory);
    }

    /**
     * 1등 경기 정보 조회 메서드
     */
    public GameInProgressWatchResponse findFirstPlaceByGameId(final Long gameId){
        List<GameHistory> histories = gameHistoryRepository.findAllByGameId(gameId);
        List<GameHistory> sortedHistories = histories.stream()
                .sorted(Comparator.comparingInt(GameHistory::getRank))
                .collect(Collectors.toList());
        return GameInProgressWatchResponse.toResponse(sortedHistories.get(0));
    }

    /**
     * gameId로 1위(rank=1) GameHistory를 조회하는 메서드
     */
    public GameHistoryResponse findFirstPlaceGameHistory(Long gameId) {
        GameHistory gameHistory = OptionalUtil.getOrElseThrow(
                gameHistoryRepository.findByGameIdAndRank(gameId, 1),
                NOT_EXIST_GAME_GISTORY_ID
        );
        Game game = OptionalUtil.getOrElseThrow(gameRepository.findById(gameId), NOT_EXIST_GAME_ID);
        return GameHistoryResponse.toResponse(game, gameHistory);
    }

    private GameStatus determineGameStatus(Game game, Set<Long> participatedGameIds) {
        boolean userParticipated = participatedGameIds.contains(game.getId());
        if (game.isInProgress() && userParticipated) {
            return GameStatus.IN_PROGRESS;
        }
        if (userParticipated) {
            return GameStatus.REGISTRATION_COMPLETE;
        }
        return GameStatus.REGISTRATION_OPEN;
    }

    /**
     * 이번주 운동 기록을 조회하는 메서드
     */
    public WeeklyExerciseResponse findWeeklyExerciseRecord(final String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(NOT_EXIST_USER_EMAIL));

        LocalDateTime weekStart = getWeekStart();
        LocalDateTime weekEnd = getWeekEnd();

        List<GameHistory> weeklyGameHistories = gameHistoryRepository.findAllByUserId(user.getId()).stream()
                .filter(gameHistory -> isGameInThisWeek(gameHistory.getGameId(), weekStart, weekEnd))
                .toList();

        double totalDistance = calculateTotalDistance(weeklyGameHistories);
        long totalMinutes = calculateTotalMinutes(weeklyGameHistories);

        return WeeklyExerciseResponse.of(totalMinutes, totalDistance);
    }

    private LocalDateTime getWeekStart() {
        return LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();
    }

    private LocalDateTime getWeekEnd() {
        return LocalDate.now()
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .atTime(23, 59, 59);
    }

    private boolean isGameInThisWeek(Long gameId, LocalDateTime weekStart, LocalDateTime weekEnd) {
        return gameRepository.findById(gameId)
                .map(game -> isWithinWeek(game.getEndAt(), weekStart, weekEnd))
                .orElse(false);
    }

    private boolean isWithinWeek(LocalDateTime endTime, LocalDateTime weekStart, LocalDateTime weekEnd) {
        return endTime.isAfter(weekStart) && endTime.isBefore(weekEnd);
    }

    private double calculateTotalDistance(List<GameHistory> gameHistories) {
        return gameHistories.stream()
                .mapToDouble(GameHistory::getCurrentDistance)
                .sum();
    }

    private long calculateTotalMinutes(List<GameHistory> gameHistories) {
        return gameHistories.stream()
                .mapToLong(this::calculateGameDuration)
                .sum();
    }

    private long calculateGameDuration(GameHistory gameHistory) {
        if (gameHistory.getStartAt() == null || gameHistory.getEndAt() == null) {
            return 0L;
        }
        return ChronoUnit.MINUTES.between(gameHistory.getStartAt(), gameHistory.getEndAt());
    }

    /**
     * 특정 년/월의 경기 기록을 조회하는 메서드
     */
    public List<GameCalendarResponse> findMonthlyGameRecords(final String email, final int year, final int month) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(NOT_EXIST_USER_EMAIL));

        List<GameHistory> userGameHistories = gameHistoryRepository.findAllByUserId(user.getId());
        List<Long> gameIds = extractGameIds(userGameHistories);

        if (gameIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Game> monthlyGames = gameRepositoryCustom.findGamesByYearAndMonth(year, month, gameIds);

        return monthlyGames.stream()
                .map(game -> createGameCalendarResponse(game, userGameHistories))
                .collect(Collectors.toList());
    }

    private List<Long> extractGameIds(List<GameHistory> gameHistories) {
        return gameHistories.stream()
                .map(GameHistory::getGameId)
                .collect(Collectors.toList());
    }

    private GameCalendarResponse createGameCalendarResponse(Game game, List<GameHistory> gameHistories) {
        GameHistory gameHistory = findGameHistoryByGameId(game.getId(), gameHistories);

        return GameCalendarResponse.builder()
                .gameId(game.getId())
                .gameDistance(game.getDistance())
                .participatedCount(game.getParticipatedCount())
                .gameStartAt(game.getStartAt())
                .gameEndAt(game.getEndAt())
                .userStartAt(gameHistory.getStartAt())
                .userEndAt(gameHistory.getEndAt())
                .firstPlacePrize(game.getFirstPlacePrize())
                .secondPlacePrize(game.getSecondPlacePrize())
                .thirdPlacePrize(game.getThirdPlacePrize())
                .myDistance(gameHistory.getCurrentDistance())
                .build();
    }

    private GameHistory findGameHistoryByGameId(Long gameId, List<GameHistory> gameHistories) {
        return gameHistories.stream()
                .filter(gh -> gh.getGameId().equals(gameId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(NOT_EXIST_GAME_GISTORY_ID));
    }

}
