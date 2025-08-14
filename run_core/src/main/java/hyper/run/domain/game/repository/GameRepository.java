package hyper.run.domain.game.repository;

import hyper.run.domain.game.entity.Game;
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

    @Query("SELECT g FROM Game g WHERE g.endAt < :targetDateTime")
    List<Game> findGamesEndedBefore(@Param("targetDateTime") LocalDateTime targetDateTime);

}
