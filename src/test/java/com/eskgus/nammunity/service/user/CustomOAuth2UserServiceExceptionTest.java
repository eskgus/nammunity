package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.domain.enums.SocialType;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.exception.SocialException;
import com.eskgus.nammunity.service.tokens.OAuth2TokensService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.tokens.OAuth2TokensDto;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static com.eskgus.nammunity.domain.enums.Fields.PASSWORD;
import static com.eskgus.nammunity.domain.enums.Fields.SOCIAL;
import static com.eskgus.nammunity.domain.enums.SocialType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomOAuth2UserServiceExceptionTest {
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
    private static final String EMAIL = Fields.EMAIL.getKey();
    private static final String EMAIL_VALUE = EMAIL + ID + "@gmail.com";
    private static final String ACCESS_TOKEN = Fields.ACCESS_TOKEN.getKey();
    private static final String REFRESH_TOKEN = Fields.REFRESH_TOKEN.getKey();
    private static final String USERNAME = Fields.USERNAME.getKey();
    private static final String USERNAME_VALUE = USERNAME + ID;

    private CustomOAuth2UserService spy;
    private OAuth2UserRequest oAuth2UserRequest;
    private ClientRegistration clientRegistrationWithNone;
    private OAuth2AccessToken oAuth2AccessToken;

    private SecurityContext context;
    private User user;
    private User existingUser;

    @Test
    public void signUpWithSocialWithNonExistentUser() {
        // given
        giveSecurityContext(null);

        giveEmailFinder(false);

        when(registrationService.encryptPassword(anyString())).thenReturn(PASSWORD.getKey() + ID);

        this.user = mock(User.class);
        when(userService.save(any(RegistrationDto.class))).thenReturn(ID);

        // when/then
        testLoadUserThrowIllegalArgumentException(userService::findById, USER_NOT_FOUND);

        verifyBeforeLinkSocialAccount(times(1), never(), never());
    }

    @Test
    public void signInWithSocialWithBannedUser() {
        testSignInWithSocialThrowOAuth2AuthenticationException(BANNED);

        // then
        verifyBeforeLinkSocialAccount(never(), times(1), never());
    }

    @Test
    public void signInWithSocialWithLockedUser() {
        testSignInWithSocialThrowOAuth2AuthenticationException(LOCKED);

        // then
        verifyBeforeLinkSocialAccount(never(), times(1), times(1));
    }

    @Test
    public void linkSocialAccountWithNonExistentUsername() {
        // given
        giveAuthentication();

        this.user = mock(User.class);

        // when/then
        testLoadUserThrowIllegalArgumentException(userService::findByUsername, USERNAME_NOT_FOUND);

        verifyAfterSignInWithSocial(never(), never());
    }

    @Test
    public void linkSocialAccountThrowBuildValidateAccessTokenUrlException() {
        testLoadUserThrowSocialException(SOCIAL, NONE);

        verifyAfterSignInWithSocial(NONE, never());
    }

    @Test
    public void linkSocialAccountThrowGetRefreshTokenException() {
        throwHttpClientErrorException();

        testLoadUserThrowSocialException(Fields.REFRESH_TOKEN, GOOGLE);

        verifyAfterSignInWithSocial(GOOGLE, times(1));
    }

    @Test
    public void linkSocialAccountWithExistentSocialAccount() {
        // given
        setUp();

        giveAuthentication();

        giveUser();
        giveExistingUser(GOOGLE);

        // when/then
        assertOAuth2AuthenticationException(SOCIAL_ACCOUNT_EXISTS);

        verifyAfterSignInWithSocial(GOOGLE, never());
    }

    @Test
    public void linkSocialAccountWithBannedUser() {
        testLinkSocialAccountThrowOAuth2AuthenticationException(BANNED);

        verifyAfterSignInWithSocial(times(1), never());
    }

    @Test
    public void linkSocialAccountWithLockedUser() {
        testLinkSocialAccountThrowOAuth2AuthenticationException(LOCKED);

        verifyAfterSignInWithSocial(times(1), times(1));
    }

    @Test
    public void unlinkSocialThrowBuildValidateAccessTokenUrlException() {
        testUnlinkSocialException(NONE, SOCIAL);
    }

    @Test
    public void unlinkSocialThrowGetRefreshTokenException() {
        throwHttpClientErrorException();

        testUnlinkSocialException(GOOGLE, Fields.REFRESH_TOKEN);
    }

    private void giveAuthentication() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(USERNAME_VALUE);

        giveSecurityContext(authentication);
    }

    private void giveUser() {
        giveUsername();
        ServiceTestUtil.giveEmail(user, EMAIL + EMAIL_VALUE);
    }

    private void giveExistingUser(SocialType socialType) {
        giveEmailFinder(true);
        when(existingUser.getSocial()).thenReturn(socialType);
    }

    private void giveSecurityContext(Authentication authentication) {
        this.context = mock(SecurityContext.class);
        SecurityContextHolder.setContext(context);

        when(context.getAuthentication()).thenReturn(authentication);
    }

    private void giveUsername() {
        this.user = ServiceTestUtil.giveUser(userService::findByUsername, String.class);
    }

    private void giveEmailFinder(boolean doesEmailExist) {
        this.existingUser = doesEmailExist ? mock(User.class) : null;
        ServiceTestUtil.giveContentFinder(userRepository::findByEmail, String.class, existingUser);
    }

    private <ParamType> void testLoadUserThrowIllegalArgumentException(Function<ParamType, User> finder,
                                                                       ExceptionMessages exceptionMessage) {
        setUp();

        throwIllegalArgumentException(finder, exceptionMessage);

        // when/then
        assertIllegalArgumentException(exceptionMessage);
    }

    private void testLoadUserThrowSocialException(Fields field, SocialType registrationId) {
        // given
        setUp();

        if (NONE.equals(registrationId)) {
            giveClientRegistration(clientRegistrationWithNone);
        }

        giveAuthentication();

        giveUser();

        giveExistingUser(NONE);

        SocialException socialException = createSocialException(user.getUsername(), field, registrationId);

        // when/then
        assertSocialException(() -> spy.loadUser(oAuth2UserRequest), socialException);
    }

    private void testSignInWithSocialThrowOAuth2AuthenticationException(ExceptionMessages exceptionMessage) {
        giveSecurityContext(null);

        giveEmailFinder(true);

        this.user = existingUser;

        when(user.getSocial()).thenReturn(NONE);

        testLoadUserThrowOAuth2AuthenticationException(exceptionMessage);
    }

    private void testLinkSocialAccountThrowOAuth2AuthenticationException(ExceptionMessages exceptionMessage) {
        giveAuthentication();

        giveUsername();

        giveEmailFinder(false);

        testLoadUserThrowOAuth2AuthenticationException(exceptionMessage);
    }

    private void testLoadUserThrowOAuth2AuthenticationException(ExceptionMessages exceptionMessage) {
        // given
        setUp();

        ServiceTestUtil.giveUsername(user, USERNAME_VALUE);

        boolean isAccountNonBanned = !BANNED.equals(exceptionMessage);
        when(bannedUsersService.isAccountNonBanned(anyString())).thenReturn(isAccountNonBanned);

        if (isAccountNonBanned) {
            when(user.isLocked()).thenReturn(true);
        }

        // when/then
        assertOAuth2AuthenticationException(exceptionMessage);
    }

    private void testUnlinkSocialException(SocialType socialType, Fields field) {
        // given
        this.user = mock(User.class);
        ServiceTestUtil.giveUsername(user, USERNAME_VALUE);

        SocialException socialException = createSocialException(user.getUsername(), field, socialType);

        VerificationMode refreshMode = SOCIAL.equals(field) ? never() : times(1);

        // when/then
        assertSocialException(
                () -> customOAuth2UserService.unlinkSocial(socialType, ACCESS_TOKEN, user), socialException);

        verify(restTemplate, refreshMode).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
        verifyUnlinkSocial(refreshMode);
        verify(restTemplate, never()).exchange(anyString(), eq(HttpMethod.POST), isNull(), eq(Map.class));
        verify(restTemplate, never()).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    private void setUp() {
        this.oAuth2UserRequest = mock(OAuth2UserRequest.class);

        this.spy = Mockito.spy(customOAuth2UserService);

        OAuth2User oAuth2User = mock(OAuth2User.class);
        doReturn(oAuth2User).when(spy).superLoadUser(any(OAuth2UserRequest.class));

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(EMAIL, EMAIL_VALUE);
        when(oAuth2User.getAttributes()).thenReturn(attributes);

        ClientRegistration.Builder googleBuilder = setRegistrationId(GOOGLE);
        ClientRegistration.Builder noneBuilder = setRegistrationId(NONE);

        ClientRegistration clientRegistrationWithGoogle = buildClientRegistration(googleBuilder);
        this.clientRegistrationWithNone = buildClientRegistration(noneBuilder);
        giveClientRegistration(clientRegistrationWithGoogle);

        this.oAuth2AccessToken = mock(OAuth2AccessToken.class);
        when(oAuth2AccessToken.getTokenValue()).thenReturn(ACCESS_TOKEN);
        when(oAuth2UserRequest.getAccessToken()).thenReturn(oAuth2AccessToken);

        Map<String, Object> additionalParameters = new HashMap<>();
        additionalParameters.put("expires_in", 3600);
        when(oAuth2UserRequest.getAdditionalParameters()).thenReturn(additionalParameters);

        additionalParameters.put("refresh_token", REFRESH_TOKEN);
    }

    private <ParamType> void throwIllegalArgumentException(Function<ParamType, User> finder, ExceptionMessages exceptionMessage) {
        ServiceTestUtil.throwIllegalArgumentException(finder, exceptionMessage);
    }

    private SocialException createSocialException(String username, Fields field, SocialType registrationId) {
        SocialType socialType = SOCIAL.equals(field) ? registrationId : null;

        return new SocialException(username, field, socialType);
    }

    private void throwHttpClientErrorException() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
    }

    private void assertIllegalArgumentException(ExceptionMessages exceptionMessage) {
        ServiceTestUtil.assertIllegalArgumentException(() -> spy.loadUser(oAuth2UserRequest), exceptionMessage);
    }

    private void assertSocialException(Executable executable, SocialException socialException) {
        SocialException exception = assertThrows(SocialException.class, executable);

        assertEquals(socialException.getUsername(), exception.getUsername());
        assertEquals(socialException.getField(), exception.getField());
        assertEquals(socialException.getRejectedValue(), exception.getRejectedValue());
    }

    private void assertOAuth2AuthenticationException(ExceptionMessages exceptionMessage) {
        OAuth2AuthenticationException exception = assertThrows(
                OAuth2AuthenticationException.class, () -> spy.loadUser(oAuth2UserRequest));

        assertEquals(OAuth2ErrorCodes.ACCESS_DENIED, exception.getError().getErrorCode());
        assertEquals(exceptionMessage.getMessage(), exception.getMessage());
    }

    private void verifyBeforeLinkSocialAccount(VerificationMode signUpMode,
                                               VerificationMode bannedMode, VerificationMode lockedMode) {
        verifyLoadUser(context);
        verifySignInWithSocial(times(1), bannedMode);
        verifySignUpWithSocial(signUpMode);
        verifyAuthenticateOAuth2User(bannedMode, lockedMode);
    }

    private void verifyAfterSignInWithSocial(SocialType registrationId, VerificationMode oAuth2TokensMode) {
        verifyLoadUser(context);
        verifyLinkSocialAccount(times(1), never(), EMAIL + EMAIL_VALUE, registrationId);
        verifyValidateSocialEmail(times(1), times(1));
        verifyCancelSocialLink(times(1));
        verifyUnlinkSocial(oAuth2TokensMode);
        verifyAuthenticateOAuth2User(never(), never());
    }

    private void verifyAfterSignInWithSocial(VerificationMode bannedMode, VerificationMode lockedMode) {
        verifyLoadUser(context);
        verifyLinkSocialAccount(times(1), bannedMode, EMAIL_VALUE, GOOGLE);
        verifyValidateSocialEmail(bannedMode, never());
        verifyAuthenticateOAuth2User(bannedMode, lockedMode);
    }

    private void verifyLoadUser(SecurityContext context) {
        verify(spy).superLoadUser(eq(oAuth2UserRequest));
        verify(oAuth2UserRequest).getClientRegistration();
        verify(oAuth2UserRequest).getAccessToken();
        verify(oAuth2AccessToken).getTokenValue();
        verify(oAuth2UserRequest, times(2)).getAdditionalParameters();
        verify(context).getAuthentication();
        verify(oAuth2TokensService, never()).save(any(OAuth2TokensDto.class));
        verify(oAuth2TokensService, never()).update(any(OAuth2TokensDto.class));
        verify(user, never()).getRole();
    }

    private void verifySignInWithSocial(VerificationMode emailMode, VerificationMode socialMode) {
        verify(userRepository, emailMode).findByEmail(eq(EMAIL_VALUE));
        verify(user, socialMode).getSocial();
        verify(user, socialMode).updateSocial(eq(GOOGLE));
    }

    private void verifySignUpWithSocial(VerificationMode signUpMode) {
        verify(registrationService, signUpMode).encryptPassword(anyString());
        verify(userService, signUpMode).save(any(RegistrationDto.class));
        verify(userService, signUpMode).findById(eq(ID));
        verify(user, never()).updateEnabled();
    }

    private void verifyLinkSocialAccount(VerificationMode findMode, VerificationMode updateMode,
                                         String email, SocialType registrationId) {
        verify(userService, findMode).findByUsername(eq(USERNAME_VALUE));
        verify(user, updateMode).updateEmail(eq(email));
        verify(user, updateMode).updateSocial(eq(registrationId));
    }

    private void verifyValidateSocialEmail(VerificationMode findMode, VerificationMode getMode) {
        verify(userRepository, findMode).findByEmail(eq(EMAIL_VALUE));
        verify(user, getMode).getEmail();
    }

    private void verifyCancelSocialLink(VerificationMode exitingUserSocialMode) {
        verify(existingUser, exitingUserSocialMode).getSocial();
    }

    private void verifyUnlinkSocial(VerificationMode oAuth2TokensMode) {
        verify(user, oAuth2TokensMode).getOAuth2Tokens();
        verify(user, never()).updateSocial(eq(NONE));
    }

    private void verifyAuthenticateOAuth2User(VerificationMode bannedMode, VerificationMode lockedMode) {
        verify(bannedUsersService, bannedMode).isAccountNonBanned(eq(USERNAME_VALUE));
        verify(user, lockedMode).isLocked();
    }

    private ClientRegistration.Builder setRegistrationId(SocialType socialType) {
        return ClientRegistration.withRegistrationId(socialType.getKey());
    }

    private ClientRegistration buildClientRegistration(ClientRegistration.Builder builder) {
        return builder
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
    }

    private void giveClientRegistration(ClientRegistration clientRegistration) {
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
    }
}
