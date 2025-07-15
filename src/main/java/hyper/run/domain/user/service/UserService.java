package hyper.run.domain.user.service;

import hyper.run.domain.user.dto.UserSignupRequest;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void save(final UserSignupRequest userSignupRequest, final String encodePassword) {
        //todo 중복 검증 (email, phone Number)
        User user = userSignupRequest.toEntity(encodePassword);
        userRepository.save(user);
    }
}
