package hyper.run.domain.user.service;

import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.dto.user.AdminUserDetailResponse;
import hyper.run.dto.user.AdminUserListResponse;
import hyper.run.exception.ErrorMessages;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserManagementService {

    private final UserRepository userRepository;

    public Page<AdminUserListResponse> getUserList(
            String searchKeyword,
            String sortBy,
            int page,
            int size
    ) {
        Sort sort = createSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> users = userRepository.searchUsers("email", searchKeyword, pageable);

        return users.map(AdminUserListResponse::from);
    }

    public AdminUserDetailResponse getUserDetail(Long userId) {
        User user = OptionalUtil.getOrElseThrow(userRepository.findById(userId), ErrorMessages.NOT_EXIST_USER_ID);
        return AdminUserDetailResponse.from(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = OptionalUtil.getOrElseThrow(userRepository.findById(userId), ErrorMessages.NOT_EXIST_USER_ID);
        userRepository.delete(user);
    }

    private Sort createSort(String sortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "id");
        }

        return switch (sortBy) {
            case "email" -> Sort.by(Sort.Direction.ASC, "email");
            case "name" -> Sort.by(Sort.Direction.ASC, "name");
            case "phoneNumber" -> Sort.by(Sort.Direction.ASC, "phoneNumber");
            case "birth" -> Sort.by(Sort.Direction.ASC, "birth");
            default -> Sort.by(Sort.Direction.DESC, "id");
        };
    }
}
