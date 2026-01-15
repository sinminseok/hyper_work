package hyper.run.domain.game.service;

import hyper.run.domain.game.dto.response.admin.AdminGameResponse;
import hyper.run.domain.game.entity.AdminGameStatus;
import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminGameService {

    private final GameRepository gameRepository;


    public Page<AdminGameResponse> findAllGames(LocalDate startDate, LocalDate endDate, AdminGameStatus status, String keyword, Pageable pageable) {

        LocalDateTime createdAfter = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime createdBefore = (endDate != null) ? endDate.atStartOfDay() : null;

        Page<Game> games = gameRepository.findGamesByCriteria(createdAfter, createdBefore, status, keyword, pageable);
        return games.map(AdminGameResponse::dtoFromGame);
    }
}
