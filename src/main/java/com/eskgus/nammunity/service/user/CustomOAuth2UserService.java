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
    private final BannedUsersService bannedUsersService;

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

        // registrationId: google, naver, kakao
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // registrationId별로 attributes, name, email 뽑는 게 달라서 굳이 CustomOAuth2User.of() 호출해서 CustomOAuth2User 생성하는 거
        CustomOAuth2User customOAuth2User = CustomOAuth2User
                .of(registrationId, oAuth2User.getAttributes());

        // accessToken이랑 refreshToken 얻어서 customOAuth2User의 attributes에 저장
        String accessToken = userRequest.getAccessToken().getTokenValue();
        int exp = (int) userRequest.getAdditionalParameters().get("expires_in");
        // accessToken은 쿠키로 저장
        customOAuth2User.getAttributes().put("accessToken", createCookie(accessToken, exp));
        // refreshToken은 그냥 저장
        customOAuth2User.getAttributes()
                .put("refreshToken", userRequest.getAdditionalParameters().get("refresh_token"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user;
        if (authentication == null) {   // 로그인 안 한 사용자면 회원가입 or 로그인
            user = signInWithSocial(customOAuth2User, registrationId);
        } else {    // 로그인 한 사용자면 소셜 연동
            String username = authentication.getName();
            user = linkToSocialAccount(customOAuth2User, registrationId, username);
        }
        // user의 locked, banned 확인
        authenticateOAuth2User(user);

        // 회원가입/로그인/소셜 연동 후 반환된 user의 username을 customOAuth2User의 attributes에 저장
        customOAuth2User.getAttributes().put("username", user.getUsername());

        // 아까 customOAuth2User의 attributes로 저장한 refreshToken을 db에 저장 or 업데이트
        // 근데 refreshToken이 안 들어오는 경우도 있어서 그때는 db에 저장/업데이트 x
        String refreshToken = (String) customOAuth2User.getAttributes().get("refreshToken");
        if (refreshToken != null) {
            saveOrUpdateRefreshToken(refreshToken, user);
        }

        // 회원가입/로그인/소셜 연동 후 반환된 user의 role, customOAuth2User의 attributes, username으로 DefaultOAuth2User 생성 후 리턴
        // (nameAttributeKey를 username으로 안 하면 principal name이 이상한 숫자로 됨 !)
        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(user.getRole().getKey())),
                customOAuth2User.getAttributes(), "username");
    }

    public void authenticateOAuth2User(User user) throws OAuth2AuthenticationException {
        log.info("authenticateOAuth2User.....");

        if (!bannedUsersService.isAccountNonBanned(user.getUsername())) {   // user의 banned 확인
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.ACCESS_DENIED),
                    "활동 정지된 계정입니다. 자세한 내용은 메일을 확인하세요.");
        } else if (user.isLocked()) {   // user의 locked 확인
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.ACCESS_DENIED),
                    "로그인에 5번 이상 실패했습니다. ID 또는 비밀번호 찾기를 하세요.");
        }
    }

    @Transactional
    public User signInWithSocial(CustomOAuth2User customOAuth2User, String registrationId) {
        log.info("signInWithSocial.....");

        Optional<User> result = userRepository.findByEmail(customOAuth2User.getEmail());
        User user;

        if (result.isPresent()) {   // customOAuth2User의 email이 users 테이블에 이미 존재하면 User는 테이블에서 찾은 user
            user = result.get();
        } else {    // email이 users 테이블에 없으면 회원가입하고, User는 지금 회원가입한 user
            String name = Character.toUpperCase(registrationId.charAt(0)) + "_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmssSSS"));
            RegistrationDto registrationDto = RegistrationDto.builder()
                    .username(name).password(encoder.encode("00000000"))
                    .nickname(name).email(customOAuth2User.getEmail())
                    .role(Role.USER).build();

            Long id = userService.signUp(registrationDto);
            user = userService.findById(id);

            // 소셜 로그인은 이메일 인증이 없으니까 가입하자마자 enabled true로 업데이트
            user.updateEnabled();
        }

        // 이제 user 뽑았으니까 리턴하고 로그인 시킬 건데, 그전에 user의 social이 none이면 registrationId로 업데이트
        if (user.getSocial().equals("none")) {
            user.updateSocial(registrationId);
        }

        return user;
    }

    @Transactional
    public User linkToSocialAccount(CustomOAuth2User customOAuth2User, String registrationId, String username) {
        log.info("linkToSocialAccount.....");

        // username: 로그인돼있는 user의 username
        User user = userService.findByUsername(username);
        // 소셜 연동할 계정 이메일
        String socialEmail = customOAuth2User.getEmail();
        Optional<User> result = userRepository.findByEmail(socialEmail);

        // 로그인돼있는 user의 email이랑 소셜 연동할 계정 email이 다르거나, 연동할 계정 email이 이미 users 테이블에 있으면 소셜 연동 요청 빠꾸
        if (!user.getEmail().equals(socialEmail) && result.isPresent()) {
            // 그 와중에 연동할 계정의 email로 이미 가입된 다른 user의 social이 none이면, 그 사람은 소셜 연동을 안 해놨다는 뜻 !
            // 근데 지금 소셜 연동 요청 보낸 사람이 요청을 보내고, 정보 제공 동의한 순간 해당 소셜과 나뮤니티가 연결돼버려서 그거 끊어줘야 하는 거
            if (result.get().getSocial().equals("none")) {
                unlinkSocial(username, registrationId,
                        ((Cookie) customOAuth2User.getAttributes().get("accessToken")).getValue());
            }
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.ACCESS_DENIED),
                    "연동할 계정을 사용 중인 다른 사용자가 있습니다.");
        }

        // 위에서 안 걸러진 email이면 소셜 연동
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
        OAuth2Tokens result = user.getOAuth2Tokens();

        if (result != null) {
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
        String refreshToken = user.getOAuth2Tokens().getRefreshToken();

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
