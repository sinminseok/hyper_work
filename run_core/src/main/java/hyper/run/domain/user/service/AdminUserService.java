package hyper.run.domain.user.service;

import hyper.run.domain.user.dto.response.UserAdminResponse;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static hyper.run.exception.ErrorMessages.NOT_EXIST_USER_EMAIL;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    /**
     * 관리자 페이지에서 사용자 조회
     */
    public Page<UserAdminResponse> searchUsers(final String searchCategory, final String keyword, final Pageable pageable){
        Page<User> userPage = userRepository.searchUsers(searchCategory,keyword,pageable);
        return userPage.map(UserAdminResponse::userToAdminUserDto);
    }

    @Transactional
    public void deleteUser(final String email){
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        userRepository.delete(user);
    }

}
