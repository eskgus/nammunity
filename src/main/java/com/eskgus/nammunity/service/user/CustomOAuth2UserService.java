package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.tokens.OAuth2Tokens;
import com.eskgus.nammunity.domain.tokens.OAuth2TokensRepository;
import com.eskgus.nammunity.domain.user.CustomOAuth2User;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.service.tokens.OAuth2TokensService;
import com.eskgus.nammunity.web.dto.tokens.OAuth2TokensDto;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
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
    private final UserService userService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naverClientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("loadUser.....");

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        CustomOAuth2User customOAuth2User = CustomOAuth2User
                .of(registrationId, oAuth2User.getAttributes());

        String accessToken = userRequest.getAccessToken().getTokenValue();
        int exp = (int) userRequest.getAdditionalParameters().get("expires_in");
        customOAuth2User.getAttributes().put("accessToken", createCookie(accessToken, exp));
        customOAuth2User.getAttributes()
                .put("refreshToken", userRequest.getAdditionalParameters().get("refresh_token"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user;
        if (authentication == null) {
            user = signInWithSocial(customOAuth2User, registrationId);
        } else {
            String username = authentication.getName();
            user = linkToSocialAccount(customOAuth2User, registrationId, username);
        }
        customOAuth2User.getAttributes().put("username", user.getUsername());

        String refreshToken = (String) customOAuth2User.getAttributes().get("refreshToken");
        if (refreshToken != null) {
            saveOrUpdateRefreshToken(refreshToken, user);
        }

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(user.getRole().getKey())),
                customOAuth2User.getAttributes(), "username");
    }

    @Transactional
    public User signInWithSocial(CustomOAuth2User customOAuth2User, String registrationId) {
        log.info("signInWithSocial.....");

        Optional<User> result = userRepository.findByEmail(customOAuth2User.getEmail());
        User user;

        if (result.isPresent()) {
            user = result.get();
        } else {
            String name = Character.toUpperCase(registrationId.charAt(0)) + "_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmssSSS"));
            RegistrationDto registrationDto = RegistrationDto.builder()
                    .username(name).password(encoder.encode("00000000"))
                    .nickname(name).email(customOAuth2User.getEmail())
                    .role(Role.USER).build();

            Long id = userService.signUp(registrationDto);
            user = userService.findById(id);
            userService.updateEnabled(user);
        }

        if (user.getSocial().equals("none")) {
            user.updateSocial(registrationId);
        }

        return user;
    }

    @Transactional
    public User linkToSocialAccount(CustomOAuth2User customOAuth2User, String registrationId, String username) {
        log.info("linkToSocialAccount.....");

        User user = userService.findByUsername(username);
        String socialEmail = customOAuth2User.getEmail();
        Optional<User> result = userRepository.findByEmail(socialEmail);

        if (!user.getEmail().equals(socialEmail) && result.isPresent()) {
            if (result.get().getSocial().equals("none")) {
                unlinkSocial(username, registrationId,
                        ((Cookie) customOAuth2User.getAttributes().get("accessToken")).getValue());
            }
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST),
                    "연동할 계정을 사용 중인 다른 사용자가 있습니다.");
        }

        Long id = user.getId();
        user.updateEmail(socialEmail);
        user.updateSocial(registrationId);

        return user;
    }

    public Cookie createCookie(String token, int exp) {
        Cookie cookie = new Cookie("access_token", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(exp);
        return cookie;
    }

    @Transactional
    public void saveOrUpdateRefreshToken(String refreshToken, User user) {
        log.info("saveOrUpdateRefreshToken.....");

        LocalDateTime exp = LocalDateTime.now().plusMonths(1);
        OAuth2TokensDto oAuth2TokensDto = OAuth2TokensDto.builder()
                .refreshToken(refreshToken).expiredAt(exp).user(user).build();
        Optional<OAuth2Tokens> result = oAuth2TokensRepository.findByUser(user);

        if (result.isPresent()) {
            oAuth2TokensService.update(oAuth2TokensDto);
        } else {
            oAuth2TokensService.save(oAuth2TokensDto);
        }
    }

    @Transactional
    public Cookie unlinkSocial(String username, String social, String accessToken) {
        log.info("unlinkSocial in service.....");

        Cookie cookie = null;
        try {
            validateAccessToken(social, accessToken);
            cookie = createCookie(null, 0);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().is4xxClientError()) {
                cookie = refreshAccessToken(social, username);
                cookie.setMaxAge(0);
                accessToken = cookie.getValue();
            }
        }

        HttpHeaders headers = new HttpHeaders();

        String url = switch (social) {
            case "google" -> "https://oauth2.googleapis.com/revoke?"
                    + "client_id=" + googleClientId
                    + "&client_secret=" + googleClientSecret
                    + "&token=" + accessToken;
            case "naver" -> "https://nid.naver.com/oauth2.0/token?"
                    + "client_id=" + naverClientId
                    + "&client_secret=" + naverClientSecret
                    + "&access_token=" + accessToken
                    + "&grant_type=delete"
                    + "&service_provider=NAVER";
            case "kakao" -> "https://kapi.kakao.com/v1/user/unlink";
            default -> throw new IllegalStateException("unexpected value");
        };
        if (social.equals("kakao")) {
            headers.set("Authorization", "Bearer " + accessToken);
        }

        HttpEntity<String> request = new HttpEntity<>(null, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        User user = userRepository.findByUsername(username).get();
        oAuth2TokensService.delete(user);
        user.updateSocial("none");

        return cookie;
    }

    public void validateAccessToken(String social, String accessToken) {
        log.info("validateAccessToken.....");

        String url = null;
        HttpHeaders headers = new HttpHeaders();

        if (social.equals("google")) {
            url = "https://oauth2.googleapis.com/tokeninfo?"
                    + "access_token=" + accessToken;
        } else {
            if (social.equals("naver")) {
                url = "https://openapi.naver.com/v1/nid/me";
            } else {
                url = "https://kapi.kakao.com/v1/user/access_token_info";
            }
            headers.set("Authorization", "Bearer " + accessToken);
        }

        HttpEntity<String> request = new HttpEntity<>(null, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
    }

    public Cookie refreshAccessToken(String social, String username) {
        log.info("refreshAccessToken.....");

        User user = userRepository.findByUsername(username).get();
        String refreshToken = oAuth2TokensRepository.findByUser(user).get()
                .getRefreshToken();

        String url = switch (social) {
            case "google" -> "https://oauth2.googleapis.com/token?"
                    + "client_id=" + googleClientId
                    + "&client_secret=" + googleClientSecret;
            case "naver" -> "https://nid.naver.com/oauth2.0/token?"
                    + "client_id=" + naverClientId
                    + "&client_secret=" + naverClientSecret;
            case "kakao" -> url = "https://kauth.kakao.com/oauth/token?"
                    + "client_id=" + kakaoClientId
                    + "&client_secret=" + kakaoClientSecret;
            default -> throw new IllegalStateException("unexpected value");
        };
        url += "&grant_type=refresh_token"
                + "&refresh_token=" + refreshToken;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, null, Map.class);

        String newAccessToken = (String) response.getBody().get("access_token");
        int exp = Integer.parseInt(response.getBody().get("expires_in").toString());
        return createCookie(newAccessToken, exp);
    }
}
