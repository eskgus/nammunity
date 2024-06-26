package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.tokens.OAuth2Tokens;
import com.eskgus.nammunity.domain.user.CustomOAuth2User;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.exception.SocialException;
import com.eskgus.nammunity.service.tokens.OAuth2TokensService;
import com.eskgus.nammunity.web.dto.tokens.OAuth2TokensDto;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final OAuth2TokensService oAuth2TokensService;
    private final UserService userService;
    private final BannedUsersService bannedUsersService;
    private final RegistrationService registrationService;

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
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        CustomOAuth2User customOAuth2User = CustomOAuth2User.of(registrationId, oAuth2User.getAttributes());

        addTokensToAttributes(userRequest, customOAuth2User);

        User user = handleUserAuthentication(customOAuth2User);

        addAttributes(customOAuth2User, "username", user.getUsername());

        saveOrUpdateRefreshToken(customOAuth2User, user);

        Collection<SimpleGrantedAuthority> authorities =
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().getKey()));
        return new DefaultOAuth2User(authorities, customOAuth2User.getAttributes(), "username");
    }

    private void addTokensToAttributes(OAuth2UserRequest userRequest, CustomOAuth2User customOAuth2User) {
        addAccessTokenToAttributes(userRequest, customOAuth2User);
        addRefreshTokenToAttributes(userRequest, customOAuth2User);
    }

    private void addAccessTokenToAttributes(OAuth2UserRequest userRequest, CustomOAuth2User customOAuth2User) {
        Cookie accessToken = createAccessTokenCookieFromRequest(userRequest, customOAuth2User);
        addAttributes(customOAuth2User, "accessToken", accessToken);
    }

    private Cookie createAccessTokenCookieFromRequest(OAuth2UserRequest userRequest, CustomOAuth2User customOAuth2User) {
        String accessToken = userRequest.getAccessToken().getTokenValue();
        int exp = (int) userRequest.getAdditionalParameters().get("expires_in");
        return createCookie(accessToken, exp);
    }

    public Cookie createCookie(String token, int exp) {
        Cookie cookie = new Cookie("access_token", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(exp);
        return cookie;
    }

    private <T> void addAttributes(CustomOAuth2User customOAuth2User, String key, T value) {
        customOAuth2User.getAttributes().put(key, value);
    }

    private void addRefreshTokenToAttributes(OAuth2UserRequest userRequest, CustomOAuth2User customOAuth2User) {
        String refreshToken = (String) userRequest.getAdditionalParameters().get("refresh_token");
        addAttributes(customOAuth2User, "refreshToken", refreshToken);
    }

    @Transactional
    private User handleUserAuthentication(CustomOAuth2User customOAuth2User) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = authentication == null
                ? signInWithSocial(customOAuth2User) : linkSocialAccount(customOAuth2User, authentication.getName());

        authenticateOAuth2User(user);

        return user;
    }

    private User signInWithSocial(CustomOAuth2User customOAuth2User) {
        Optional<User> result = userRepository.findByEmail(customOAuth2User.getEmail());
        User user = result.orElseGet(() -> signUpWithSocial(customOAuth2User));

        if (user.getSocial().equals("none")) {
            user.updateSocial(customOAuth2User.getRegistrationId());
        }

        return user;
    }

    private User signUpWithSocial(CustomOAuth2User customOAuth2User) {
        RegistrationDto registrationDto = createRegistrationDto(customOAuth2User);
        Long id = userService.save(registrationDto);

        User user = userService.findById(id);
        user.updateEnabled();

        return user;
    }

    private RegistrationDto createRegistrationDto(CustomOAuth2User customOAuth2User) {
        String name = createName(customOAuth2User.getRegistrationId());
        String password = registrationService.encryptPassword("00000000");
        return RegistrationDto.builder()
                .username(name).password(password).nickname(name).email(customOAuth2User.getEmail()).role(Role.USER)
                .build();
    }

    private String createName(String registrationId) {
        return Character.toUpperCase(registrationId.charAt(0)) + "_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmssSSS"));
    }

    private User linkSocialAccount(CustomOAuth2User customOAuth2User, String username) {
        User user = userService.findByUsername(username);

        validateSocialEmail(customOAuth2User, user);

        user.updateEmail(customOAuth2User.getEmail());
        user.updateSocial(customOAuth2User.getRegistrationId());
        return user;
    }

    private void validateSocialEmail(CustomOAuth2User customOAuth2User, User user) {
        String socialEmail = customOAuth2User.getEmail();

        Optional<User> result = userRepository.findByEmail(socialEmail);
        result.ifPresent(existingUser -> {
            if (!user.getEmail().equals(socialEmail)) {
                cancelSocialLink(existingUser, customOAuth2User, user);
                throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.ACCESS_DENIED),
                        "연동할 계정을 사용 중인 다른 사용자가 있습니다.");
            }
        });
    }

    private void cancelSocialLink(User existingUser, CustomOAuth2User customOAuth2User, User user) {
        if (existingUser.getSocial().equals("none")) {
            String accessToken = ((Cookie) customOAuth2User.getAttributes().get("accessToken")).getValue();
            unlinkSocial(customOAuth2User.getRegistrationId(), accessToken, user);
        }
    }

    @Transactional
    public Cookie unlinkSocial(String social, String accessToken, User user) {
        Cookie cookie = validateAccessTokenAndRequestSocialUnlink(social, accessToken, user);

        updateNonSocial(user);

        return cookie;
    }

    private Cookie validateAccessTokenAndRequestSocialUnlink(String social, String accessToken, User user) {
        Cookie cookie;
        try {
            validateAccessToken(social, accessToken, user.getUsername());
            cookie = createCookie(null, 0);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().is4xxClientError()) {
                cookie = refreshAccessToken(user, social);
                cookie.setMaxAge(0);
                accessToken = cookie.getValue();
            } else {
                throw ex;
            }
        }

        requestSocialUnlink(social, accessToken, user.getUsername());
        return cookie;
    }

    private void validateAccessToken(String social, String accessToken, String username) {
        HttpHeaders headers = new HttpHeaders();
        String url = buildValidateAccessTokenUrl(social, accessToken, headers, username);

        HttpEntity<String> request = new HttpEntity<>(null, headers);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.exchange(url, HttpMethod.GET, request, String.class);
    }

    private String buildValidateAccessTokenUrl(String social, String accessToken, HttpHeaders headers, String username) {
        switch (social) {
            case "google":
                return "https://oauth2.googleapis.com/tokeninfo?access_token=" + accessToken;
            case "naver":
                headers.set("Authorization", "Bearer " + accessToken);
                return "https://openapi.naver.com/v1/nid/me";
            case "kakao":
                headers.set("Authorization", "Bearer " + accessToken);
                return "https://kapi.kakao.com/v1/user/access_token_info";
            default: throw new SocialException(username, "social", social);
        }
    }

    private Cookie refreshAccessToken(User user, String social) {
        ResponseEntity<Map> response = requestAccessTokenRefresh(user, social);
        return createAccessTokenCookieFromResponse(response);
    }

    private ResponseEntity<Map> requestAccessTokenRefresh(User user, String social) {
        String refreshToken = getRefreshToken(user);
        String url = buildAccessTokenRefreshUrl(social, user.getUsername(), refreshToken);

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(url, HttpMethod.POST, null, Map.class);
    }

    private String getRefreshToken(User user) {
        OAuth2Tokens oAuth2Tokens = user.getOAuth2Tokens();
        if (oAuth2Tokens == null) {
            throw new SocialException(user.getUsername(), "refreshToken", null);
        }
        return oAuth2Tokens.getRefreshToken();
    }

    private String buildAccessTokenRefreshUrl(String social, String username, String refreshToken) {
        StringBuilder urlBuilder = buildBaseUrl(social, username);
        return urlBuilder.append("&grant_type=refresh_token")
                .append("&refresh_token=")
                .append(refreshToken).toString();
    }

    private StringBuilder buildBaseUrl(String social, String username) {
        return switch (social) {
            case "google" -> new StringBuilder("https://oauth2.googleapis.com/token?")
                    .append("client_id=").append(googleClientId)
                    .append("&client_secret=").append(googleClientSecret);
            case "naver" -> new StringBuilder("https://nid.naver.com/oauth2.0/token?")
                    .append("client_id=").append(naverClientId)
                    .append("&client_secret=").append(naverClientSecret);
            case "kakao" -> new StringBuilder()
                    .append("client_id=").append(kakaoClientId)
                    .append("&client_secret=").append(kakaoClientSecret);
            default -> throw new SocialException(username, "social", social);
        };
    }

    private Cookie createAccessTokenCookieFromResponse(ResponseEntity<Map> response) {
        String newAccessToken = (String) response.getBody().get("access_token");
        int exp = Integer.parseInt(response.getBody().get("expires_in").toString());
        return createCookie(newAccessToken, exp);
    }

    private void requestSocialUnlink(String social, String accessToken, String username) {
        String url = buildSocialUnlinkUrl(social, accessToken, username).toString();
        HttpHeaders headers = generateSocialUnlinkHeaders(social, accessToken);

        HttpEntity<String> request = new HttpEntity<>(null, headers);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    }
    private StringBuilder buildSocialUnlinkUrl(String social, String accessToken, String username) {
        return switch (social) {
            case "google" -> new StringBuilder("https://oauth2.googleapis.com/revoke?")
                    .append("client_id=").append(googleClientId)
                    .append("&client_secret=").append(googleClientSecret)
                    .append("&token=").append(accessToken);
            case "naver" -> new StringBuilder("https://nid.naver.com/oauth2.0/token?")
                    .append("client_id=").append(naverClientId)
                    .append("&client_secret=").append(naverClientSecret)
                    .append("&access_token=").append(accessToken)
                    .append("&grant_type=delete")
                    .append("&service_provider=NAVER");
            case "kakao" -> new StringBuilder("https://kapi.kakao.com/v1/user/unlink");
            default -> throw new SocialException(username, "social", social);
        };
    }

    private HttpHeaders generateSocialUnlinkHeaders(String social, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        if (social.equals("kakao")) {
            headers.set("Authorization", "Bearer " + accessToken);
        }
        return headers;
    }

    private void updateNonSocial(User user) {
        user.updateSocial("none");
    }

    private void authenticateOAuth2User(User user) throws OAuth2AuthenticationException {
        if (!bannedUsersService.isAccountNonBanned(user.getUsername())) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.ACCESS_DENIED),
                    "활동 정지된 계정입니다. 자세한 내용은 메일을 확인하세요.");
        } else if (user.isLocked()) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.ACCESS_DENIED),
                    "로그인에 5번 이상 실패했습니다. ID 또는 비밀번호 찾기를 하세요.");
        }
    }

    @Transactional
    private void saveOrUpdateRefreshToken(CustomOAuth2User customOAuth2User, User user) {
        String refreshToken = (String) customOAuth2User.getAttributes().get("refreshToken");
        if (refreshToken != null) {
            OAuth2TokensDto oAuth2TokensDto = createOAuth2TokensDto(refreshToken, user);
            OAuth2Tokens oAuth2Tokens = user.getOAuth2Tokens();
            if (oAuth2Tokens != null) {
                oAuth2TokensService.update(oAuth2TokensDto);
            } else {
                oAuth2TokensService.save(oAuth2TokensDto);
            }
        }
    }

    private OAuth2TokensDto createOAuth2TokensDto(String refreshToken, User user) {
        LocalDateTime exp = LocalDateTime.now().plusMonths(1);
        return OAuth2TokensDto.builder()
                .refreshToken(refreshToken).expiredAt(exp).user(user).build();
    }
}
