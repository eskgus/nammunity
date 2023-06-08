package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.user.CustomOAuth2User;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final BCryptPasswordEncoder encoder;
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        ClientRegistration clientRegistration = userRequest.getClientRegistration();
        String registrationId = clientRegistration.getRegistrationId();

        CustomOAuth2User customOAuth2User = CustomOAuth2User
                .of(registrationId, oAuth2User.getAttributes());

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

        return user;
    }
}
