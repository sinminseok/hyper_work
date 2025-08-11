package hyper.run.domain.game.repository;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    @Query("SELECT g FROM Game g WHERE g.endAt > :now")
    List<Game> findUpcomingGames(@Param("now") LocalDateTime now);

    @Query("SELECT g FROM Game g " +
            "WHERE g.gameDate = :targetDate " +
            "AND FUNCTION('HOUR', g.startAt) = :targetHour")
    List<Game> findGamesByDateAndHour(@Param("targetDate") LocalDate targetDate, @Param("targetHour") int targetHour);

    @Query("SELECT g FROM Game g WHERE " +
            "(:createdAfter IS NULL OR g.createdAt >= :createdAfter) AND " +
            "(:createdBefore IS NULL OR g.createdAt < :createdBefore) AND " +
            "(:status IS NULL OR g.status = :status) AND " +
            "(:keyword IS NULL OR g.name LIKE %:keyword%)")
    Page<Game> findGamesByCriteria(
            @Param("createdAfter") LocalDateTime createdAfter,
            @Param("createdBefore") LocalDateTime createdBefore,
            @Param("status") GameStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
