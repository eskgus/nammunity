package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.domain.enums.SocialType;
import com.eskgus.nammunity.domain.tokens.OAuth2Tokens;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.service.tokens.OAuth2TokensService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.tokens.OAuth2TokensDto;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.eskgus.nammunity.domain.enums.Fields.*;
import static com.eskgus.nammunity.domain.enums.SocialType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomOAuth2UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuth2TokensService oAuth2TokensService;

    @Mock
    private UserService userService;

    @Mock
    private BannedUsersService bannedUsersService;

    @Mock
    private RegistrationService registrationService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    private static final Long ID = 1L;
    private static final Role ROLE = Role.USER;
    private static final String EMAIL = Fields.EMAIL.getKey();
    private static final String EMAIL_VALUE = EMAIL + ID + "@gmail.com";
    private static final String ACCESS_TOKEN = Fields.ACCESS_TOKEN.getKey();
    private static final String REFRESH_TOKEN = Fields.REFRESH_TOKEN.getKey();
    private static final String USERNAME = Fields.USERNAME.getKey();
    private static final String USERNAME_VALUE = USERNAME + ID;
    private static final int EXP = 3600;

    private CustomOAuth2UserService spy;
    private OAuth2UserRequest oAuth2UserRequest;
    private OAuth2AccessToken oAuth2AccessToken;
    private User user;
    private OAuth2Tokens oAuth2Token;

    @Test
    public void signUpWithSocial() {
        when(registrationService.encryptPassword(anyString())).thenReturn(PASSWORD.getKey() + ID);

        when(userService.save(any(RegistrationDto.class))).thenReturn(ID);

        giveUser(userService::findById, Long.class);
        this.oAuth2Token = null;
        testSignUpOrSignIn(false, NONE);
    }

    @Test
    public void signInWithSocialFirstTime() {
        this.user = mock(User.class);
        this.oAuth2Token = null;
        testSignUpOrSignIn(true, NONE);
    }

    @Test
    public void signInWithSocial() {
        this.user = mock(User.class);
        this.oAuth2Token = mock(OAuth2Tokens.class);
        testSignUpOrSignIn(true, GOOGLE);
    }


    @Test
    public void linkSocialAccountWithSameEmail() {
        testLinkSocialAccount(true);
    }

    @Test
    public void linkSocialAccountWithDifferentEmail() {
        testLinkSocialAccount(false);
    }

    @Test
    public void createCookie() {
        // given
        // when
        Cookie result = customOAuth2UserService.createCookie(ACCESS_TOKEN, EXP);

        // then
        assertEquals("access_token", result.getName());
        assertEquals(ACCESS_TOKEN, result.getValue());
        assertEquals("/", result.getPath());
        assertTrue(result.isHttpOnly());
        assertEquals(EXP, result.getMaxAge());
    }

    @Test
    public void unlinkGoogleWithValidatedAccessToken() {
        testUnlinkSocialWithValidatedAccessToken(GOOGLE);
    }

    @Test
    public void unlinkNaverWithValidatedAccessToken() {
        testUnlinkSocialWithValidatedAccessToken(NAVER);
    }

    @Test
    public void unlinkKakaoWithValidatedAccessToken() {
        testUnlinkSocialWithValidatedAccessToken(KAKAO);
    }

    @Test
    public void unlinkGoogleWithRefreshedAccessToken() {
        testUnlinkSocialWithRefreshedAccessToken(GOOGLE);
    }

    @Test
    public void unlinkNaverWithRefreshedAccessToken() {
        testUnlinkSocialWithRefreshedAccessToken(NAVER);
    }

    @Test
    public void unlinkKakaoWithRefreshedAccessToken() {
        testUnlinkSocialWithRefreshedAccessToken(KAKAO);
    }

    private <ParamType> void giveUser(Function<ParamType, User> finder, Class<ParamType> paramType) {
        this.user = ServiceTestUtil.giveUser(finder, paramType);
    }

    private void giveSocial(SocialType socialType) {
        when(user.getSocial()).thenReturn(socialType);
    }

    private Authentication giveAuthentication() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(USERNAME_VALUE);

        return authentication;
    }

    private SecurityContext giveSecurityContext(Authentication authentication) {
        SecurityContext context = mock(SecurityContext.class);
        SecurityContextHolder.setContext(context);

        when(context.getAuthentication()).thenReturn(authentication);

        return context;
    }

    private void giveEmailFinder(boolean doesEmailExist) {
        User existingUser = doesEmailExist ? user : null;
        ServiceTestUtil.giveContentFinder(userRepository::findByEmail, String.class, existingUser);
    }

    private void giveUsername() {
        ServiceTestUtil.giveUsername(user, USERNAME_VALUE);
    }

    private void giveOAuth2Token(OAuth2Tokens oAuth2Token) {
        when(user.getOAuth2Tokens()).thenReturn(oAuth2Token);

        if (oAuth2Token == null) {
            when(oAuth2TokensService.save(any(OAuth2TokensDto.class))).thenReturn(ID);
        } else {
            when(oAuth2TokensService.update(any(OAuth2TokensDto.class))).thenReturn(ID);
        }
    }

    private void giveOAuth2Token() {
        this.oAuth2Token = mock(OAuth2Tokens.class);
        when(oAuth2Token.getRefreshToken()).thenReturn(REFRESH_TOKEN);
        when(user.getOAuth2Tokens()).thenReturn(oAuth2Token);
    }

    private Map<String, Object> giveResponseBody(ResponseEntity<Map> responseMap) {
        Map<String, Object> responseBody = mock(Map.class);
        when(responseMap.getBody()).thenReturn(responseBody);

        when(responseBody.get("access_token")).thenReturn(ACCESS_TOKEN);
        when(responseBody.get("expires_in")).thenReturn(EXP);

        return responseBody;
    }

    private <ResponseType> ResponseEntity<ResponseType> giveRestTemplate(HttpMethod httpMethod,
                                                                         Class<ResponseType> responseType) {
        ResponseEntity<ResponseType> responseEntity = mock(ResponseEntity.class);
        when(restTemplate.exchange(anyString(), eq(httpMethod), any(), eq(responseType))).thenReturn(responseEntity);

        return responseEntity;
    }

    private void testSignUpOrSignIn(boolean doesEmailExist, SocialType socialType) {
        // given
        setUp();

        giveSocial(socialType);

        // when/then
        testLoadUser(null, doesEmailExist);

        VerificationMode mode = doesEmailExist ? never() : times(1);
        verify(user, mode).updateEnabled();
        verify(user).getSocial();
    }

    private void testLinkSocialAccount(boolean doesEmailExist) {
        // given
        setUp();

        Authentication authentication = giveAuthentication();

        giveUser(userService::findByUsername, String.class);

        this.oAuth2Token = null;

        if (doesEmailExist) {
            when(user.getEmail()).thenReturn(EMAIL_VALUE);
        }

        // when/then
        testLoadUser(authentication, doesEmailExist);

        verify(userService).findByUsername(eq(USERNAME_VALUE));
    }

    private void setUp() {
        this.oAuth2UserRequest = mock(OAuth2UserRequest.class);

        this.spy = Mockito.spy(customOAuth2UserService);

        OAuth2User oAuth2User = mock(OAuth2User.class);
        doReturn(oAuth2User).when(spy).superLoadUser(any(OAuth2UserRequest.class));

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(EMAIL, EMAIL_VALUE);
        when(oAuth2User.getAttributes()).thenReturn(attributes);

        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(GOOGLE.getKey())
                .redirectUri("{baseUrl}/{action}/oauth2/code/{registrationId}")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope("read:user")
                .authorizationUri("https://example.com/login/oauth/authorize")
                .tokenUri("https://example.com/login/oauth/access_token")
                .userInfoUri("https://api.example.com/user")
                .userNameAttributeName("id")
                .clientName("Client Name")
                .clientId("client-id-2")
                .clientSecret("client-secret").build();
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);

        this.oAuth2AccessToken = mock(OAuth2AccessToken.class);
        when(oAuth2AccessToken.getTokenValue()).thenReturn(ACCESS_TOKEN);
        when(oAuth2UserRequest.getAccessToken()).thenReturn(oAuth2AccessToken);

        Map<String, Object> additionalParameters = new HashMap<>();
        additionalParameters.put("expires_in", EXP);
        when(oAuth2UserRequest.getAdditionalParameters()).thenReturn(additionalParameters);

        additionalParameters.put("refresh_token", REFRESH_TOKEN);
    }

    private void testLoadUser(Authentication authentication, boolean doesEmailExist) {
        // given
        SecurityContext context = giveSecurityContext(authentication);

        giveEmailFinder(doesEmailExist);

        giveUsername();
        when(bannedUsersService.isAccountNonBanned(anyString())).thenReturn(true);

        when(user.isLocked()).thenReturn(false);

        giveOAuth2Token(oAuth2Token);

        when(user.getRole()).thenReturn(Role.USER);

        // when
        OAuth2User result = spy.loadUser(oAuth2UserRequest);

        // then
        assertLoadUserResult(result);

        verifyOAuth2UserRequest();
        verifyAuthentication(context);
        verifyUpdateSocial();
    }

    private void testUnlinkSocialWithValidatedAccessToken(SocialType socialType) {
        // given
        this.user = mock(User.class);
        giveUsername();

        giveRestTemplate(HttpMethod.GET, String.class);

        giveRestTemplate(HttpMethod.POST, String.class);

        // when/then
        testUnlinkSocial(socialType, never());
    }

    private void testUnlinkSocialWithRefreshedAccessToken(SocialType socialType) {
        // given
        this.user = mock(User.class);
        giveUsername();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        giveOAuth2Token();

        ResponseEntity<Map> responseMap = giveRestTemplate(HttpMethod.POST, Map.class);
        Map<String, Object> responseBody = giveResponseBody(responseMap);

        giveRestTemplate(HttpMethod.POST, String.class);

        // when/then
        testUnlinkSocial(socialType, times(1));

        verify(responseMap, times(2)).getBody();
        verify(responseBody).get(eq("access_token"));
        verify(responseBody).get(eq("expires_in"));
    }

    private void testUnlinkSocial(SocialType socialType, VerificationMode refreshMode) {
        // when
        Cookie result = customOAuth2UserService.unlinkSocial(socialType, ACCESS_TOKEN, user);

        // then
        assertEquals(0, result.getMaxAge());

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
        verify(user, refreshMode).getOAuth2Tokens();
        verify(restTemplate, refreshMode).exchange(anyString(), eq(HttpMethod.POST), isNull(), eq(Map.class));
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    private void assertLoadUserResult(OAuth2User result) {
        // authorities
        assertEquals(ROLE.getKey(), getAuthority(result));

        // attributes
        assertEquals(EMAIL_VALUE, result.getAttribute(EMAIL));
        assertEquals(ACCESS_TOKEN, getAccessToken(result).getValue());
        assertEquals(REFRESH_TOKEN, result.getAttribute(REFRESH_TOKEN));
        assertEquals(USERNAME_VALUE, result.getAttribute(USERNAME));

        // name
        assertEquals(USERNAME_VALUE, result.getName());
    }

    private void verifyOAuth2UserRequest() {
        verify(spy).superLoadUser(eq(oAuth2UserRequest));
        verify(oAuth2UserRequest).getClientRegistration();
        verify(oAuth2UserRequest).getAccessToken();
        verify(oAuth2AccessToken).getTokenValue();
        verify(oAuth2UserRequest, times(2)).getAdditionalParameters();
    }

    private void verifyAuthentication(SecurityContext context) {
        verify(context).getAuthentication();
        verify(userRepository).findByEmail(eq(EMAIL_VALUE));
        verify(bannedUsersService).isAccountNonBanned(eq(USERNAME_VALUE));
        verify(user).isLocked();
        verify(user).getOAuth2Tokens();
        verify(user).getRole();
    }

    private void verifyUpdateSocial() {
        boolean updateSocial = oAuth2Token == null;
        VerificationMode mode = updateSocial ? times(1) : never();

        verify(user, mode).updateSocial(eq(GOOGLE));

        if (updateSocial) {
            verify(oAuth2TokensService).save(any(OAuth2TokensDto.class));
        } else {
            verify(oAuth2TokensService).update(any(OAuth2TokensDto.class));
        }
    }

    private String getAuthority(OAuth2User result) {
        return result.getAuthorities().iterator().next().getAuthority();
    }

    private Cookie getAccessToken(OAuth2User result) {
        return result.getAttribute(ACCESS_TOKEN);
    }
}
