package hyper.run.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import hyper.run.auth.filter.CustomLoginAuthenticationFilter;
import hyper.run.auth.filter.JwtAuthenticationFilter;
import hyper.run.auth.handler.LoginFailureHandler;
import hyper.run.auth.handler.LoginSuccessHandler;
import hyper.run.auth.service.JwtService;
import hyper.run.auth.service.CustomUserDetailsService;
import hyper.run.auth.service.TokenCustomService;
import hyper.run.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import static hyper.run.auth.constants.AuthUrlPatterns.*;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenCustomService tokenCustomService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(formLogin -> formLogin.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, GET_AUTH_WHITELIST).permitAll()
                        .requestMatchers(HttpMethod.POST, POST_AUTH_WHITELIST).permitAll()
                        .requestMatchers(HttpMethod.PATCH, POST_AUTH_WHITELIST).permitAll()
                        .requestMatchers(NOT_AUTH_URL).permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/logs-viewer.html").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .anyRequest().authenticated()
                );

        http.addFilterAfter(customJsonUsernamePasswordAuthenticationFilter(http), LogoutFilter.class);
        http.addFilterBefore(jwtAuthenticationProcessingFilter(), CustomLoginAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CustomLoginAuthenticationFilter customJsonUsernamePasswordAuthenticationFilter(HttpSecurity http) throws Exception {
        CustomLoginAuthenticationFilter customJsonUsernamePasswordLoginFilter = new CustomLoginAuthenticationFilter(objectMapper);
        customJsonUsernamePasswordLoginFilter.setAuthenticationManager(authenticationManager(http));
        customJsonUsernamePasswordLoginFilter.setAuthenticationSuccessHandler(loginSuccessHandler());
        customJsonUsernamePasswordLoginFilter.setAuthenticationFailureHandler(loginFailureHandler());
        return customJsonUsernamePasswordLoginFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder())
                .and()
                .build();

    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationProcessingFilter() {
        return new JwtAuthenticationFilter(tokenCustomService, jwtService, userRepository);
    }

    @Bean
    public LoginSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler(jwtService, userRepository);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}