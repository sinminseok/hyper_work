package hyper.run.domain.user.repository.admin;

import hyper.run.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    Page<User> searchUsers(String searchCategory,String keyword,Pageable pageable);
}
