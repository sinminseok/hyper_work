package hyper.run.domain.repository;


import hyper.run.domain.entity.AdminUser;
import hyper.run.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findByAdmin(AdminUser admin);


}
