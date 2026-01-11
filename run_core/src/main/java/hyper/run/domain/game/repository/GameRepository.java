package hyper.run.domain.game.repository;

import hyper.run.domain.game.entity.Game;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    @Query("SELECT g FROM Game g WHERE g.endAt > :now")
    List<Game> findUpcomingGames(@Param("now") LocalDateTime now);

    @Query("SELECT g FROM Game g " +
            "WHERE FUNCTION('DATE', g.startAt) = :targetDate " +
            "AND FUNCTION('HOUR', g.startAt) = :targetHour")
    List<Game> findGamesByDateAndHour(@Param("targetDate") LocalDate targetDate, @Param("targetHour") int targetHour);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM Game g WHERE g.id = :gameId")
    Optional<Game> findByIdForUpdate(@Param("gameId") Long gameId);

    @Query("SELECT g FROM Game g WHERE g.startAt > :now ORDER BY g.totalPrize DESC LIMIT 3")
    List<Game> findTop3UpcomingGamesByTotalPrize(@Param("now") LocalDateTime now);

    // 관리자용 페이지네이션 조회
    Page<Game> findAllByCreateDateTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);
}
