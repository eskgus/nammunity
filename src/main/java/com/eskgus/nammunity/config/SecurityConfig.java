package com.eskgus.nammunity.config;

import com.eskgus.nammunity.domain.user.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig{
    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable().headers().frameOptions().disable()
                .and()
                .authorizeHttpRequests((request) -> request
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**"))
                        .permitAll()
                        .requestMatchers("/", "/posts/read/**", "/api/users/**", "/users/**", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/api/posts/**", "/posts/save/**", "/posts/update/**").hasRole(Role.USER.name())
                        .anyRequest().authenticated())
                .httpBasic(withDefaults());
        return http.build();
    }
}
