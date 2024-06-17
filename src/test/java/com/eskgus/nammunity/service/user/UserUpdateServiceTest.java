package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.exception.CustomValidException;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.web.dto.user.EmailUpdateDto;
import com.eskgus.nammunity.web.dto.user.NicknameUpdateDto;
import com.eskgus.nammunity.web.dto.user.PasswordUpdateDto;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserUpdateServiceTest {
    @Mock
    private BCryptPasswordEncoder encoder;

    @Mock
    private UserService userService;

    @Mock
    private RegistrationService registrationService;

    @Mock
    private CustomOAuth2UserService customOAuth2UserService;

    @Mock
    private PrincipalHelper principalHelper;

    @InjectMocks
    private UserUpdateService userUpdateService;

    @Test
    public void updatePassword() {
        // given
        PasswordUpdateDto requestDto = createPasswordUpdateDto(true, true);

        Pair<Principal, User> pair = givePrincipal("password", requestDto.getOldPassword());
        Principal principal = pair.getFirst();
        User user = pair.getSecond();
        when(user.getId()).thenReturn(1L);

        when(encoder.matches(anyString(), anyString())).thenReturn(true);

        when(registrationService.encryptPassword(anyString())).thenReturn("encryptedPassword");

        // when
        Long result = userUpdateService.updatePassword(requestDto, principal);

        // then
        assertEquals(user.getId(), result);

        verifyPrincipalAndMatches(principal, times(1), requestDto.getOldPassword());
        verifyAfterMatches(times(1), requestDto.getPassword(), user);
    }

    @Test
    public void updatePasswordWithoutPrincipal() {
        // given
        PasswordUpdateDto requestDto = createPasswordUpdateDto(true, true);

        User user = mock(User.class);

        when(principalHelper.getUserFromPrincipal(null, true))
                .thenThrow(IllegalArgumentException.class);

        // when/then
        assertThrows(IllegalArgumentException.class, () -> userUpdateService.updatePassword(requestDto, null));

        verifyPrincipalAndMatches(null, never(), "dummyOldPassword");
        verifyAfterMatches(never(), "dummyPassword", user);
    }

    @Test
    public void updatePasswordWithNonMatchingOldPassword() {
        assertThrowsAndVerifyUpdatePassword(true, true, false);
    }

    @Test
    public void updatePasswordWithSameOldAndNewPassword() {
        assertThrowsAndVerifyUpdatePassword(false, true, true);
    }

    @Test
    public void updatePasswordWithNonMatchingConfirmPassword() {
        assertThrowsAndVerifyUpdatePassword(true, false, true);
    }

    @Test
    public void updateNickname() {
        // given
        NicknameUpdateDto requestDto = createNicknameUpdateDto(true);

        Pair<Principal, User> pair = givePrincipal("nickname", "nickname");
        Principal principal = pair.getFirst();
        User user = pair.getSecond();

        when(user.getId()).thenReturn(1L);

        when(userService.existsByNickname(anyString())).thenReturn(false);

        // when
        Long result = userUpdateService.updateNickname(requestDto, principal);

        // then
        assertEquals(user.getId(), result);

        verifyUpdateNickname(principal, Pair.of(requestDto, user),
                times(1), times(1));
    }

    @Test
    public void updateNicknameWithoutPrincipal() {
        when(principalHelper.getUserFromPrincipal(null, true))
                .thenThrow(IllegalArgumentException.class);

        assertThrowsAndVerifyUpdateNickname(true, null, mock(User.class), never());
    }

    @Test
    public void updateNicknameWithSameOldAndNewNickname() {
        Pair<Principal, User> pair = givePrincipal("nickname", "nickname");

        assertThrowsAndVerifyUpdateNickname(false, pair.getFirst(), pair.getSecond(), never());
    }

    @Test
    public void updateNicknameWithExistentNickname() {
        Pair<Principal, User> pair = givePrincipal("nickname", "nickname");

        when(userService.existsByNickname(anyString())).thenReturn(true);

        assertThrowsAndVerifyUpdateNickname(true, pair.getFirst(), pair.getSecond(), times(1));
    }

    @Test
    public void updateEmailWhenUserIsNotEnabled() {
        User user = createMockedUser(false);

        testUpdateEmail(user, never());
    }

    @Test
    public void updateEmailWhenUserIsEnabled() {
        User user = giveUser(false);

        testUpdateEmail(user, times(1));
    }

    @Test
    public void updateEmailWithoutPrincipal() {
        User user = createMockedUser(null);

        when(principalHelper.getUserFromPrincipal(null, true))
                .thenThrow(IllegalArgumentException.class);

        Tokens token = mock(Tokens.class);

        assertThrowsAndVerifyUpdateEmail(user, null, token, never());
    }

    @Test
    public void updateEmailWithSameOldAndNewEmail() {
        User user = createMockedUser(true);

        assertThrowsAndVerifyUpdateEmail(false, user, never());
    }

    @Test
    public void updateEmailWithExistentEmail() {
        User user = giveUser(true);

        assertThrowsAndVerifyUpdateEmail(true, user, times(1));
    }

    @Test
    public void updateEmailWithNonExistentUserId() {
        User user = giveUser(false);

        Principal principal = givePrincipal(user);

        Tokens token = giveToken(user);

        doThrow(IllegalArgumentException.class).when(registrationService).sendToken(anyLong(), anyString(), anyString());

        assertThrowsAndVerifyUpdateEmail(user, principal, token, times(1));
    }

    @Test
    public void deleteRegularUser() {
        Cookie cookie = mock(Cookie.class);

        testDeleteUser(cookie, "none", never());
    }

    @Test
    public void deleteSocialUser() {
        Cookie cookie = createMockedCookie();
        when(customOAuth2UserService.unlinkSocial(anyString(), anyString(), any(User.class))).thenReturn(cookie);

        testDeleteUser(cookie, "naver", times(1));
    }

    @Test
    public void deleteUserWithoutPrincipal() {
        User user = giveUser("none");

        when(principalHelper.getUserFromPrincipal(null, true))
                .thenThrow(IllegalArgumentException.class);

        assertThrowsAndVerifyDeleteUser(null, user, never());
    }

    @Test
    public void deleteUserWithNonExistentUserId() {
        User user = giveUser("none");

        Principal principal = givePrincipal(user);

        doThrow(IllegalArgumentException.class).when(userService).delete(anyLong());

        assertThrowsAndVerifyDeleteUser(principal, user, times(1));
    }

    private void assertThrowsAndVerifyUpdatePassword(boolean isPasswordValid, boolean isPasswordConfirmed,
                                                     boolean matches) {
        // given
        PasswordUpdateDto requestDto = createPasswordUpdateDto(isPasswordValid, isPasswordConfirmed);

        Pair<Principal, User> pair = givePrincipal("password", requestDto.getOldPassword());
        Principal principal = pair.getFirst();
        User user = pair.getSecond();

        when(encoder.matches(anyString(), anyString())).thenReturn(matches);

        // when/then
        assertThrows(CustomValidException.class, () -> userUpdateService.updatePassword(requestDto, principal));

        verifyCommon(principal, requestDto.getOldPassword(), user);
    }

    private PasswordUpdateDto createPasswordUpdateDto(boolean isPasswordValid, boolean isPasswordConfirmed) {
        String password = "password";
        String oldPassword = isPasswordValid ? "old" + password : password;
        String confirmPassword = isPasswordConfirmed ? password : "confirm" + password;

        return PasswordUpdateDto.builder()
                .oldPassword(oldPassword).password(password).confirmPassword(confirmPassword).build();
    }

    private Pair<Principal, User> givePrincipal(String field, String value) {
        Principal principal = mock(Principal.class);
        User user = mock(User.class);
        if (field.equals("password")) {
            when(user.getPassword()).thenReturn(value);
        } else if (field.equals("nickname")) {
            when(user.getNickname()).thenReturn(value);
        }
        when(principalHelper.getUserFromPrincipal(principal, true)).thenReturn(user);

        return Pair.of(principal, user);
    }

    private void verifyCommon(Principal principal, String oldPassword, User user) {
        verifyPrincipalAndMatches(principal, times(1), oldPassword);
        verifyAfterMatches(never(), "dummyPassword", user);
    }

    private void verifyPrincipalAndMatches(Principal principal, VerificationMode mode, String oldPassword) {
        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(encoder, mode).matches(eq(oldPassword), anyString());
    }

    private void verifyAfterMatches(VerificationMode mode, String password, User user) {
        verify(registrationService, mode).encryptPassword(eq(password));
        verify(user, mode).updatePassword(anyString());
    }

    private void assertThrowsAndVerifyUpdateNickname(boolean isNicknameValid, Principal principal,
                                                     User user, VerificationMode existsMode) {
        // given
        NicknameUpdateDto requestDto = createNicknameUpdateDto(isNicknameValid);

        Class<? extends Throwable> exceptedType
                = principal != null? CustomValidException.class : IllegalArgumentException.class;

        // when/then
        assertThrows(exceptedType, () -> userUpdateService.updateNickname(requestDto, principal));

        verifyUpdateNickname(principal, Pair.of(requestDto, user), existsMode, never());
    }

    private NicknameUpdateDto createNicknameUpdateDto(boolean isNicknameValid) {
        String nickname = "nickname";
        String newNickname = isNicknameValid ? "new" + nickname : nickname;

        return new NicknameUpdateDto(newNickname);
    }

    private void verifyUpdateNickname(Principal principal, Pair<NicknameUpdateDto, User> pair,
                                      VerificationMode existsMode, VerificationMode updateMode) {
        NicknameUpdateDto requestDto = pair.getFirst();

        verifyBeforeUpdateNickname(principal, existsMode, requestDto);
        verify(pair.getSecond(), updateMode).updateNickname(eq(requestDto.getNickname()));
    }

    private void verifyBeforeUpdateNickname(Principal principal,
                                            VerificationMode existsMode, NicknameUpdateDto requestDto) {
        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(userService, existsMode).existsByNickname(eq(requestDto.getNickname()));
    }

    private User giveUser(boolean exists) {
        User user = createMockedUser(true);
        when(userService.existsByEmail(anyString())).thenReturn(exists);

        return user;
    }

    private User createMockedUser(Boolean isEnabled) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getEmail()).thenReturn("email@naver.com");
        if (isEnabled != null) {
            when(user.isEnabled()).thenReturn(isEnabled);
        }

        return user;
    }

    private void testUpdateEmail(User user, VerificationMode mode) {
        // given
        EmailUpdateDto requestDto = createEmailUpdateDto(true, user.getEmail());
        Pair<EmailUpdateDto, User> pair = Pair.of(requestDto, user);

        Principal principal = givePrincipal(user);

        Tokens token = giveToken(user);

        // when
        Long result = userUpdateService.updateEmail(requestDto, principal);

        // then
        assertEquals(user.getId(), result);

        verify(principalHelper).getUserFromPrincipal(principal, true);

        VerificationMode times = times(1);
        verifyUser(pair, times, mode, mode);
        verifyAfterUser(token, pair, times, times);
    }

    private void assertThrowsAndVerifyUpdateEmail(User user, Principal principal, Tokens token, VerificationMode mode) {
        // given
        EmailUpdateDto requestDto = createEmailUpdateDto(true, user.getEmail());
        Pair<EmailUpdateDto, User> pair = Pair.of(requestDto, user);

        // when/then
        assertThrows(IllegalArgumentException.class, () -> userUpdateService.updateEmail(requestDto, principal));

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verifyUser(pair, mode, mode, mode);
        verifyAfterUser(token, pair, mode, never());
    }

    private void assertThrowsAndVerifyUpdateEmail(boolean isEmailValid, User user, VerificationMode mode) {
        // given
        EmailUpdateDto requestDto = createEmailUpdateDto(isEmailValid, user.getEmail());
        Pair<EmailUpdateDto, User> pair = Pair.of(requestDto, user);

        Principal principal = givePrincipal(user);

        Tokens token = mock(Tokens.class);

        // when/then
        assertThrows(CustomValidException.class, () -> userUpdateService.updateEmail(requestDto, principal));

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verifyUser(pair, times(1), mode, never());
        verifyAfterUser(token, pair, never(), never());
    }

    private EmailUpdateDto createEmailUpdateDto(boolean isEmailValid, String email) {
        String newEmail = isEmailValid ? "new" + email : email;

        return new EmailUpdateDto(newEmail);
    }

    private Principal givePrincipal(User user) {
        Principal principal = mock(Principal.class);
        when(principalHelper.getUserFromPrincipal(principal, true)).thenReturn(user);

        return principal;
    }

    private Tokens giveToken(User user) {
        Tokens token = mock(Tokens.class);
        List<Tokens> tokens = Collections.singletonList(token);
        when(user.getTokens()).thenReturn(tokens);

        return token;
    }

    private void verifyUser(Pair<EmailUpdateDto, User> pair, VerificationMode enabledMode,
                            VerificationMode existsMode, VerificationMode updateEnabledMode) {
        User user = pair.getSecond();

        verify(user, enabledMode).isEnabled();
        verify(userService, existsMode).existsByEmail(eq(pair.getFirst().getEmail()));
        verify(user, updateEnabledMode).updateEnabled();
    }

    private void verifyAfterUser(Tokens token, Pair<EmailUpdateDto, User> pair,
                                 VerificationMode tokenMode, VerificationMode updateEmailMode) {
        verifyToken(token, pair, tokenMode);
        verify(pair.getSecond(), updateEmailMode).updateEmail(eq(pair.getFirst().getEmail()));
    }

    private void verifyToken(Tokens token, Pair<EmailUpdateDto, User> pair, VerificationMode tokenMode) {
        verify(token, tokenMode).updateExpiredAt(any(LocalDateTime.class));
        verify(registrationService, tokenMode)
                .sendToken(eq(pair.getSecond().getId()), eq(pair.getFirst().getEmail()), eq("update"));
    }

    private Cookie createMockedCookie() {
        Cookie cookie = mock(Cookie.class);
        when(cookie.getName()).thenReturn("access_token");
        when(cookie.getValue()).thenReturn(null);
        when(cookie.getPath()).thenReturn("/");
        when(cookie.isHttpOnly()).thenReturn(true);
        when(cookie.getMaxAge()).thenReturn(0);

        return cookie;
    }

    private User giveUser(String social) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getSocial()).thenReturn(social);

        return user;
    }

    private void testDeleteUser(Cookie cookie, String social, VerificationMode mode) {
        // given
        User user = giveUser(social);

        Principal principal = givePrincipal(user);

        String accessToken = "accessToken";

        // when
        HttpHeaders result = userUpdateService.deleteUser(principal, accessToken);

        // then
        if (social.equals("none")) {
            assertNull(result);
        } else {
            assertNotNull(result);
        }

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verifySocial(user, cookie, mode);
        verify(userService).delete(eq(user.getId()));
    }

    private void assertThrowsAndVerifyDeleteUser(Principal principal, User user, VerificationMode mode) {
        // given
        Cookie cookie = mock(Cookie.class);

        String accessToken = "accessToken";

        // when/then
        assertThrows(IllegalArgumentException.class, () -> userUpdateService.deleteUser(principal, accessToken));

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verifySocial(user, cookie, never());
        verify(userService, mode).delete(eq(user.getId()));
    }

    private void verifySocial(User user, Cookie cookie, VerificationMode mode) {
        verify(customOAuth2UserService, mode).unlinkSocial(eq(user.getSocial()), anyString(), eq(user));
        verify(cookie, mode).getName();
    }
}
