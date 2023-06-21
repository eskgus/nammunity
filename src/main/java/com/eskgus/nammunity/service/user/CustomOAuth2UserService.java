package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.tokens.OAuth2Tokens;
import com.eskgus.nammunity.domain.tokens.OAuth2TokensRepository;
import com.eskgus.nammunity.domain.user.CustomOAuth2User;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.service.tokens.OAuth2TokensService;
import com.eskgus.nammunity.web.dto.tokens.OAuth2TokensDto;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final BCryptPasswordEncoder encoder;
    private final UserRepository userRepository;
    private final OAuth2TokensRepository oAuth2TokensRepository;
    private final OAuth2TokensService oAuth2TokensService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("CustomOAuth2UserService.....");

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        CustomOAuth2User customOAuth2User = CustomOAuth2User
                .of(registrationId, oAuth2User.getAttributes());
        customOAuth2User.getAttributes()
                .put("refreshToken", userRequest.getAdditionalParameters().get("refresh_token"));
        customOAuth2User.getAttributes().put("accessToken", createCookie(userRequest));

        User user = saveOrUpdate(customOAuth2User, registrationId);
        customOAuth2User.getAttributes().put("username", user.getUsername());

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(user.getRole().getKey())),
                customOAuth2User.getAttributes(), "username");
    }

    public User saveOrUpdate(CustomOAuth2User customOAuth2User, String registrationId) {
        Optional<User> result = userRepository.findByEmail(customOAuth2User.getEmail());
        User user;

        if (result.isPresent()) {
            user = result.get();
        } else {
            String name = Character.toUpperCase(registrationId.charAt(0)) + "_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmssSSS"));

            user = User.builder().username(name)
                    .password(encoder.encode("00000000"))
                    .nickname(name)
                    .email(customOAuth2User.getEmail())
                    .role(Role.USER).build();
            user.updateEnabled();
        }

        if (!user.isSocial()) {
            user.updateSocial();
            userRepository.save(user);
        }

        String refreshToken = (String) customOAuth2User.getAttributes().get("refreshToken");
        if (refreshToken != null) {
            LocalDateTime exp = LocalDateTime.now().plusMonths(1);
            OAuth2TokensDto oAuth2TokensDto = OAuth2TokensDto.builder()
                    .refreshToken(refreshToken).expiredAt(exp).user(user).build();

            Optional<OAuth2Tokens> tokenResult = oAuth2TokensRepository.findByUser(user);
            if (tokenResult.isPresent()) {
                oAuth2TokensService.update(oAuth2TokensDto);
            } else {
                oAuth2TokensService.save(oAuth2TokensDto);
            }
        }

        return user;
    }

    public Cookie createCookie(OAuth2UserRequest userRequest) {
        String token = userRequest.getAccessToken().getTokenValue();
        int exp = (int) userRequest.getAdditionalParameters().get("expires_in");

        Cookie cookie = new Cookie("access_token", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(exp);
        return cookie;
    }
}
