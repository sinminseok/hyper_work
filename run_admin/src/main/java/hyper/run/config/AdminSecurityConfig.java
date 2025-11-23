package hyper.run.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import hyper.run.domain.security.AdminJwtAuthenticationFilter;
import hyper.run.domain.security.AdminLoginFailureHandler;
import hyper.run.domain.security.AdminLoginSuccessHandler;
import hyper.run.domain.security.CustomLoginFilter;
import hyper.run.domain.service.AdminUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AdminSecurityConfig {

    private final AdminJwtAuthenticationFilter jwtAuthenticationFilter;
    private final AdminCorsConfig corsConfig;
    private final AdminLoginSuccessHandler adminLoginSuccessHandler;
    private final AdminLoginFailureHandler adminLoginFailureHandler;
    private final AdminUserDetailsService adminUserDetailsService;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterAt(customLoginFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, BasicAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        //  OPTIONS 요청은 인증 없이 항상 최우선으로 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/v1/api/admin/auth").permitAll()
                        .requestMatchers("/v1/api/admin/**").hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(adminUserDetailsService); // 사용자 정보 가져와서
        authenticationProvider.setPasswordEncoder(passwordEncoder); // provider 안에서 암호화된 비밀변호 db 와 비교
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public CustomLoginFilter customLoginFilter() {
        CustomLoginFilter filter = new CustomLoginFilter(objectMapper); // ObjectMapper는 DI 받아야 함
        AuthenticationManager authenticationManager = authenticationManager(passwordEncoder());
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationSuccessHandler(adminLoginSuccessHandler);
        filter.setAuthenticationFailureHandler(adminLoginFailureHandler);
        return filter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
