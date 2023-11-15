package com.eskgus.nammunity.config;

import com.eskgus.nammunity.handler.*;
import com.eskgus.nammunity.domain.user.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private CustomAuthenticationProvider customAuthenticationProvider;

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient =
                new DefaultAuthorizationCodeTokenResponseClient();

        OAuth2AccessTokenResponseHttpMessageConverter tokenResponseHttpMessageConverter =
                new OAuth2AccessTokenResponseHttpMessageConverter();
        tokenResponseHttpMessageConverter.setAccessTokenResponseConverter(new CustomTokenResponseConverter());

        RestTemplate restTemplate = new RestTemplate(Arrays.asList(
                new FormHttpMessageConverter(), tokenResponseHttpMessageConverter));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

        accessTokenResponseClient.setRestOperations(restTemplate);
        return accessTokenResponseClient;
    }

    @Bean
    public CustomAuthenticationFailureHandler authenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }

    @Bean
    public CustomAuthenticationSuccessHandler authenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }

    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable().headers().frameOptions().disable()
                .and()
                .authorizeHttpRequests(request -> request
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**"))
                        .permitAll()
                        .requestMatchers("/", "/posts/read/**",
                                "/api/users", "/api/users/confirm/**", "/api/users/sign-in",
                                "/users/sign-up/**", "/users/sign-in", "/users/confirm-email", "/users/find/**",
                                "/users/activity-history/**", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/api/posts/**", "/posts/save/**", "/posts/update/**",
                                "/api/users/update/**", "/api/users/delete", "/users/my-page/**",
                                "/api/comments/**", "/api/likes/**", "/api/reports/content")
                        .hasAnyRole(Role.USER.name(), Role.ADMIN.name())
                        .requestMatchers("/admin/my-page/content-report/**",
                                "/api/reports/content/selected-delete", "/api/reports/process").hasRole(Role.ADMIN.name())
                        .anyRequest().authenticated())
                .httpBasic(withDefaults())
                .formLogin(login -> login
                        .loginPage("/users/sign-in")
                        .successHandler(authenticationSuccessHandler())
                        .failureHandler(authenticationFailureHandler())
                        .permitAll())
                .oauth2Login(login -> login
                        .loginPage("/users/sign-in")
                        .tokenEndpoint().accessTokenResponseClient(accessTokenResponseClient())
                        .and()
                        .successHandler(new OAuth2AuthenticationSuccessHandler())
                        .failureHandler(new OAuth2AuthenticationFailureHandler()))
                .logout(logout -> logout
                        .logoutUrl("/users/sign-out")
                        .logoutSuccessHandler(new CustomLogoutSuccessHandler())
                        .permitAll());
        return http.build();
    }
}
