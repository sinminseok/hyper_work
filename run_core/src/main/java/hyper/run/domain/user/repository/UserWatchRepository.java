package hyper.run.domain.user.repository;

import hyper.run.domain.user.entity.UserWatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserWatchRepository extends JpaRepository<UserWatch, Long> {

    Optional<UserWatch> findByUserId(Long userId);
}
