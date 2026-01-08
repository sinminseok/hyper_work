package hyper.run.domain.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security UserDetails 커스텀 구현체 (관리자용)
 * SecurityContext에 adminUserId와 email을 모두 저장하여
 * 매번 DB 조회 없이 관리자 정보를 가져올 수 있습니다.
 */
@Getter
public class CustomAdminUserDetails implements UserDetails {

    private final Long adminUserId;
    private final String email;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomAdminUserDetails(Long adminUserId, String email, String role) {
        this.adminUserId = adminUserId;
        this.email = email;
        this.role = role;
        this.authorities = List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;  // 관리자는 JWT 기반이므로 password 불필요
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
