package hyper.run.domain.game.service;

import hyper.run.domain.game.dto.request.GameApplyRequest;
import hyper.run.domain.game.dto.response.admin.AdminGameResponse;
import hyper.run.domain.game.dto.response.GameHistoryResponse;
import hyper.run.domain.game.dto.response.GameInProgressWatchResponse;
import hyper.run.domain.game.dto.response.GameResponse;
import hyper.run.domain.game.entity.AdminGameStatus;
import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.entity.GameStatus;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.domain.game.repository.admin.GameRepositoryCustom;
import hyper.run.domain.game.service.scheduler.GameScheduler;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static hyper.run.exception.ErrorMessages.*;

@Service
@RequiredArgsConstructor
public class GameService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final GameScheduler gameScheduler;
    private final GameRepositoryCustom gameRepositoryCustom;

    public void testStart(Long gameId){
        Game game = gameRepository.findById(gameId).get();
        gameScheduler.startGameByTest(game);
    }

    /**
     * 게임 참가 신청 메서드
     * Game 의 총 참여자 인원수를 1 증가하고, 사용자의 경기 참여 내역을 추가한다
     */
    @Transactional
    public void applyGame(final String userEmail, final GameApplyRequest request){
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(userEmail), NOT_EXIST_USER_EMAIL);
        user.validateCouponAmount();
        Game game = OptionalUtil.getOrElseThrow(gameRepository.findById(request.getGameId()), NOT_EXIST_GAME_ID);
        gameHistoryRepository.save(request.toGameHistory(user.getId(), game.getDistance()));
        game.increaseParticipatedCount();
        user.decreaseCoupon();
    }

    /**
     * 참가 철회 메서드
     */
    @Transactional
    public void cancelGame(final String email, final Long gameId){
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        Game game = OptionalUtil.getOrElseThrow(gameRepository.findById(gameId), NOT_EXIST_GAME_ID);
        game.decreaseParticipatedCount();
        GameHistory gameHistory = OptionalUtil.getOrElseThrow(gameHistoryRepository.findByUserIdAndGameId(user.getId(), gameId), NOT_EXIST_GAME_GISTORY_ID);
        gameHistoryRepository.delete(gameHistory); // GameHistory 삭제
        user.increaseCoupon(); // 사용자 쿠폰 1개 증가
    }

    /**
     * 참가 취소 메서드
     */
    @Transactional
    public void giveUpGame(final String email, final Long gameId){
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        Game game = OptionalUtil.getOrElseThrow(gameRepository.findById(gameId), NOT_EXIST_GAME_ID);
        game.decreaseParticipatedCount();
        GameHistory gameHistory = OptionalUtil.getOrElseThrow(gameHistoryRepository.findByUserIdAndGameId(user.getId(), gameId), NOT_EXIST_GAME_GISTORY_ID);
        gameHistoryRepository.delete(gameHistory); // GameHistory 삭제
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
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(NOT_EXIST_USER_EMAIL));
        return gameHistoryRepository.findAllByUserId(user.getId()).stream()
                .map(gameHistory -> {
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
     * 1등 경기 정보 조회 메서드
     */
    public GameInProgressWatchResponse findFirstPlaceByGameId(final Long gameId){
        List<GameHistory> histories = gameHistoryRepository.findAllByGameId(gameId);
        List<GameHistory> sortedHistories = histories.stream()
                .sorted(Comparator.comparingInt(GameHistory::getRank))
                .collect(Collectors.toList());
        return GameInProgressWatchResponse.toResponse(sortedHistories.get(0));
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
    /** 관리자 페이지
     * 예정,진행,종료된 경기 모두 조회
     */
    public Page<AdminGameResponse> findAllGames(LocalDate startDate, LocalDate endDate, AdminGameStatus status, String keyword, Pageable pageable) {

        LocalDateTime createdAfter = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime createdBefore = (endDate != null) ? endDate.plusDays(1).atStartOfDay() : null;

        Page<Game> games = gameRepositoryCustom.findGamesByCriteria(createdAfter, createdBefore, status, keyword, pageable);
        return games.map(AdminGameResponse::dtoFromGame);
    }

}
