package hyper.run.auth.service;


import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static hyper.run.auth.constants.AuthConstants.ROLE;


@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), "존재하지 않는 이메일 입니다.");

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(ROLE))
        );
    }
}
