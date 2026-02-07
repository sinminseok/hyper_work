package hyper.run.domain.game.service;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameDistance;
import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.game.event.GameExpiredCleanupEvent;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.dto.game.AdminGameListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameManagementService {

    private final GameRepository gameRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Page<AdminGameListResponse> getGameList(
            String status,
            LocalDate startDate,
            LocalDate endDate,
            GameType gameType,
            GameDistance gameDistance,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDateTime"));

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        Page<Game> games = gameRepository.findGamesForAdmin(
                startDateTime,
                endDateTime,
                gameType,
                gameDistance,
                status,
                pageable
        );

        return games.map(AdminGameListResponse::from);
    }

    public Game getGameDetail(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("경기를 찾을 수 없습니다."));
    }

    /**
     * 오늘 날짜 기준으로 만료된 경기 중 참여자가 0인 경기를 삭제한다.
     */
    @Transactional
    public void cleanupExpiredGamesWithNoParticipants() {
        LocalDate today = LocalDate.now();
        List<Game> expiredGames = gameRepository.findExpiredGamesWithNoParticipants(today);

        if (expiredGames.isEmpty()) {
            return;
        }

        List<Long> gameIds = expiredGames.stream()
                .map(Game::getId)
                .toList();

        eventPublisher.publishEvent(GameExpiredCleanupEvent.from(today, gameIds));
    }
}
