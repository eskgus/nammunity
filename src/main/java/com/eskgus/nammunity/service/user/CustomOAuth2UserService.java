package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.user.CustomOAuth2User;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Log4j2
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final BCryptPasswordEncoder encoder;
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("userRequest...");
        log.info(userRequest);

        OAuth2User oAuth2User = super.loadUser(userRequest);

        ClientRegistration clientRegistration = userRequest.getClientRegistration();
        String registrationId = clientRegistration.getRegistrationId();
        log.info("registration id: " + registrationId);
        log.info("---------------------------------");

        CustomOAuth2User customOAuth2User = CustomOAuth2User
                .of(registrationId, oAuth2User.getAttributes());
        log.info("customOAuth2User attributes...");
        customOAuth2User.getAttributes().forEach((k, v) ->
                log.info(k + ": " + v));
        log.info("---------------------------------");

        User user = saveOrUpdate(customOAuth2User, registrationId);
        log.info("user saved...");
        log.info("username: " + user.getUsername());
        log.info("---------------------------------");

        customOAuth2User.getAttributes().put("username", user.getUsername());
        log.info("add username to customOAuth2User attributes...");
        customOAuth2User.getAttributes().forEach((k, v) ->
                log.info(k + ": " + v));
        log.info("---------------------------------");

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(user.getRole().getKey())),
                customOAuth2User.getAttributes(), "username");
    }

    public User saveOrUpdate(CustomOAuth2User customOAuth2User, String registrationId) {
        // TODO: 해당 이메일로 가입된 기존 나뮤니티 사용자가 있을 때: 로그인 처리

        // TODO: 해당 이메일로 가입된 기존 나뮤니티 사용자가 없을 때: 회원가입 후 로그인 처리
        int num = userRepository.findAll().size();

        User user = User.builder().username(registrationId + "_" + String.format("%03d", num + 1))
                .password(encoder.encode("00000000"))
                .nickname(registrationId + "_" + String.format("%03d", num + 1))
                .email(customOAuth2User.getEmail())
                .role(Role.USER).build();
        user.updateEnabled();

        return userRepository.save(user);
    }
}
