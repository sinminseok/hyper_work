package hyper.run.domain.game.repository;

import hyper.run.domain.game.entity.GameHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface GameHistoryRepository extends MongoRepository<GameHistory, String> {
    Optional<GameHistory> findByUserIdAndGameId(Long userId, Long gameId);

    List<GameHistory> findAllByUserId(Long userId);

    List<GameHistory> findAllByGameId(Long gameId);
}