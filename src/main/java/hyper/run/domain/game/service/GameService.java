package hyper.run.domain.game.service;

import hyper.run.domain.game.dto.request.GameApplyRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(userEmail), "존재하지 않는 사용자 이메일 입니다.");
        user.validateCouponAmount();
        gameHistoryRepository.save(request.toGameHistory(user.getId()));
        Game game = OptionalUtil.getOrElseThrow(gameRepository.findById(request.getGameId()), "존재하지 않는 경기 아이디입니다.");
        game.increaseParticipatedCount();
        user.decreaseCoupon();
    }

    /**
     * 참가 철회 메서드
     */
    @Transactional
    public void cancelGame(final String email, final Long gameId){
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), "존재하지 않는 사용자 이메일 입니다.");
        Game game = OptionalUtil.getOrElseThrow(gameRepository.findById(gameId), "존재하지 않는 게임입니다.");
        game.decreaseParticipatedCount();
        GameHistory gameHistory = OptionalUtil.getOrElseThrow(gameHistoryRepository.findByUserIdAndGameId(user.getId(), gameId), "존재하지 않는 게임 참여 기록입니다.");
        gameHistoryRepository.delete(gameHistory);
        user.increaseCoupon();
    }

    /**
     * todo 성능 개선 필요
     * 예정된 경기를 조회한다. 이때 자신이 이미 신청한 경기와 신청하지 않은 경기를 구분한다.
     */
    public List<GameResponse> findGames(final String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 이메일 입니다."));
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
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 이메일 입니다."));

        return gameHistoryRepository.findAllByUserId(user.getId()).stream()
                .map(gameHistory -> {
                    Game game = OptionalUtil.getOrElseThrow(gameRepository.findById(gameHistory.getGameId()), "존재하지 않는 게임 ID 입니다."
                    );
                    return GameHistoryResponse.toResponse(game, gameHistory);
                })
                .toList();
    }

    public GameResponse findById(final Long gameId){
        Game game = OptionalUtil.getOrElseThrow(gameRepository.findById(gameId), "존재하지 않는 게임 ID 입니다.");
        return GameResponse.toResponse(game, GameStatus.PARTICIPATE_FINISH);
    }

    private GameStatus determineGameStatus(Game game, Long userId) {
        if (game.isInProgress()) {
            return GameStatus.IN_PROGRESS;
        }
        return isUserParticipated(game.getId(), userId)
                ? GameStatus.REGISTRATION_COMPLETE
                : GameStatus.REGISTRATION_OPEN;
    }

    private boolean isUserParticipated(Long gameId, Long userId) {
        return gameHistoryRepository.findByUserIdAndGameId(userId, gameId).isPresent();
    }

}
