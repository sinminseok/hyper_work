package hyper.run.domain.game.service;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameDistance;
import hyper.run.domain.game.entity.GameType;
import hyper.run.domain.game.repository.GameRepository;
import hyper.run.dto.game.AdminGameListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameManagementService {

    private final GameRepository gameRepository;

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
}
