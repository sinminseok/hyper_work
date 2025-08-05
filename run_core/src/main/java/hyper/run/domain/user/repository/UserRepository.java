package hyper.run.domain.user.repository;

import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.admin.UserRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByRefreshToken(String refreshToken);

}
