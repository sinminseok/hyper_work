package hyper.run.domain.service;

import hyper.run.domain.entity.AdminUser;
import hyper.run.domain.repository.AdminUserRepository;
import hyper.run.domain.security.CustomUserDetails;
import hyper.run.utils.OptionalUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
@AllArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;
    private final String ROLE = "ROLE";

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AdminUser admin = OptionalUtil.getOrElseThrow(adminUserRepository.findByEmail(email),"존재하지 않는 관리자입니다.");

        return new CustomUserDetails(
                admin.getId(),
                admin.getEmail(),
                admin.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(ROLE))
        );
    }
}
