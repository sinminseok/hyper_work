package hyper.run.domain.game.service;

import hyper.run.domain.game.dto.request.GameApplyRequest;
import hyper.run.domain.game.dto.response.AdminGameResponse;
import hyper.run.domain.game.dto.response.GameHistoryResponse;
import hyper.run.domain.game.dto.response.GameResponse;
import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.game.entity.GameStatus;
import hyper.run.domain.game.repository.GameHistoryRepository;
import hyper.run.domain.game.repository.GameRepository;
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static hyper.run.exception.ErrorMessages.*;

@Service
@RequiredArgsConstructor
public class GameService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final GameHistoryRepository gameHistoryRepository;


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
     * todo 성능 개선 필요
     * 예정된 경기를 조회한다. 이때 자신이 이미 신청한 경기와 신청하지 않은 경기를 구분한다.
     * todo 경기 진행중인데, 참여하지 않는 경기는 보여주지 않아야된다.
     */
    public List<GameResponse> findGames(final String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new IllegalArgumentException(NOT_EXIST_USER_EMAIL));
        LocalDateTime now = LocalDateTime.now();
        return gameRepository.findUpcomingGames(now).stream()
                .filter(game -> game.isInProgress() || game.isNotYetStart())
                .map(game -> GameResponse.toResponse(game, determineGameStatus(game, user.getId())))
                .collect(Collectors.toList());
    }

    /**
     * 자신의 종료된 경기 참여 내역을 모두 조회한다.
     */
    public List<GameHistoryResponse> findMyGameHistories(final String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(NOT_EXIST_USER_EMAIL));
        return gameHistoryRepository.findAllByUserId(user.getId()).stream()
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

    private GameStatus determineGameStatus(Game game, Long userId) {
        var isParticipated = isUserParticipated(game.getId(), userId);
        if (game.isInProgress() && isParticipated) {
            return GameStatus.IN_PROGRESS;
        }
        return isParticipated
                ? GameStatus.REGISTRATION_COMPLETE
                : GameStatus.REGISTRATION_OPEN;
    }

    private boolean isUserParticipated(Long gameId, Long userId) {
        return gameHistoryRepository.findByUserIdAndGameId(userId, gameId).isPresent();
    }
    /** 관리자 페이지
     * 예정,진행,종료된 경기 모두 조회
     */
    public Page<AdminGameResponse> findAllGames(LocalDate startDate, LocalDate endDate,GameStatus status, String keyword,Pageable pageable){
        // 1. LocalDate를 LocalDateTime으로 변환합니다. (시간 범위를 포함하기 위함)
        // startDate가 null이면 null을, 아니면 그 날의 시작 시간(00:00:00)으로 변환
        LocalDateTime createdAfter = (startDate != null) ? startDate.atStartOfDay() : null;

        // endDate가 null이면 null을, 아니면 그 다음 날의 시작 시간(00:00:00)으로 변환
        // (이렇게 해야 endDate 당일의 23:59:59까지 포함됩니다)
        LocalDateTime createdBefore = (endDate != null) ? endDate.plusDays(1).atStartOfDay() : null;

        // 2. 변환된 값으로 리포지토리를 호출합니다.
        Page<Game> games = gameRepository.findGamesByCriteria(createdAfter, createdBefore, status,keyword,pageable);

        return games.map(AdminGameResponse::gamesToAdminGamesDto);
    }

}
