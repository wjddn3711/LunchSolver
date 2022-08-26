package com.app.lunchsolver.config.auth;

import com.app.lunchsolver.enums.Role;
import com.app.lunchsolver.service.CustomOAuth2UserService;
import com.app.lunchsolver.service.CustomOAuth2UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration //시큐리티 활성화 -> 기본 스프링 필터 체인에 등록
public class SecurityConfig{
    @Autowired
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .csrf().disable()
                .headers().frameOptions().disable()
                .and()
                .authorizeRequests()
                .antMatchers("/","/css/**","/images/**",
                        "/js/**","h2-console/**","/user/api/**").permitAll() // 해당 url을 가진 경우 모두 허용
                .antMatchers("/api/v1/**").hasRole(Role.USER.name())
                .anyRequest().authenticated()
                .and()
                .logout()
                .logoutSuccessUrl("/")
                .and()
                .oauth2Login()
                .defaultSuccessUrl("/main")  // 로그인 성공시 url
//                .failureUrl("/") // 로그인 실패시 url
                .userInfoEndpoint()
                .userService(customOAuth2UserService);
        return http.build();
    }
}
