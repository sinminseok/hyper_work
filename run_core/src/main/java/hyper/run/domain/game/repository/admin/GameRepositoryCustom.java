package hyper.run.domain.game.repository.admin;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface GameRepositoryCustom {
    Page<Game> findGamesByCriteria(
            LocalDateTime createdAfter,
            LocalDateTime createdBefore,
            GameStatus status,
            String keyword,
            Pageable pageable
    );
}
