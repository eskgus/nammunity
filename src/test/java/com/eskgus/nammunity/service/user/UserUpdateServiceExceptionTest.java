package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.domain.enums.SocialType;
import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.exception.CustomValidException;
import com.eskgus.nammunity.exception.SocialException;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.user.EmailUpdateDto;
import com.eskgus.nammunity.web.dto.user.NicknameUpdateDto;
import com.eskgus.nammunity.web.dto.user.PasswordUpdateDto;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.data.util.Pair;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static com.eskgus.nammunity.domain.enums.Fields.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserUpdateServiceExceptionTest {
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

    private static final Long ID = 1L;
    private static final Fields OLD_PASSWORD = Fields.OLD_PASSWORD;
    private static final Fields PASSWORD = Fields.PASSWORD;
    private static final Fields CONFIRM_PASSWORD = Fields.CONFIRM_PASSWORD;
    private static final String PASSWORD_VALUE = PASSWORD.getKey() + ID;
    private static final Fields EMAIL = Fields.EMAIL;
    private static final String EMAIL_PREFIX = EMAIL.getKey() + ID;
    private static final String EMAIL_SUFFIX = "@naver.com";
    private static final String EMAIL_VALUE = EMAIL_PREFIX + EMAIL_SUFFIX;
    private static final String NICKNAME_VALUE = Fields.NICKNAME.getKey() + ID;
    private static final String ACCESS_TOKEN = Fields.ACCESS_TOKEN.getKey();
    private static final SocialType SOCIAL_TYPE = SocialType.GOOGLE;

    @Test
    public void updatePasswordWithAnonymousUser() {
        testUpdatePasswordThrowsPrincipalException(null, UNAUTHORIZED);
    }

    @Test
    public void updatePasswordWithNonExistentUsername() {
        Principal principal = mock(Principal.class);
        testUpdatePasswordThrowsPrincipalException(principal, USERNAME_NOT_FOUND);
    }

    @Test
    public void updatePasswordWithMismatchOldPassword() {
        testUpdatePasswordThrowsCustomValidException(OLD_PASSWORD, false, OLD_PASSWORD_MISMATCH);
    }

    @Test
    public void updatePasswordWithInvalidNewPassword() {
        testUpdatePasswordThrowsCustomValidException(PASSWORD, true, INVALID_NEW_PASSWORD);
    }

    @Test
    public void updatePasswordWithMismatchConfirmPassword() {
        testUpdatePasswordThrowsCustomValidException(CONFIRM_PASSWORD, true, CONFIRM_PASSWORD_MISMATCH);
    }

    @Test
    public void updateEmailWithAnonymousUser() {
        testUpdateEmailThrowsPrincipalException(null, UNAUTHORIZED);
    }

    @Test
    public void updateEmailWithNonExistentUsername() {
        Principal principal = mock(Principal.class);
        testUpdateEmailThrowsPrincipalException(principal, USERNAME_NOT_FOUND);
    }

    @Test
    public void updateEmailWithInvalidNewEmail() {
        testUpdateEmailThrowsCustomValidException(true, INVALID_NEW_EMAIL);
    }

    @Test
    public void updateEmailWithExistentEmail() {
        giveChecker(userService::existsByEmail, true);

        testUpdateEmailThrowsCustomValidException(false, EMAIL_EXISTS);
    }

    @Test
    public void updateEmailWithNonExistentUser() {
        // given
        Pair<Principal, User> pair = givePrincipal();
        Principal principal = pair.getFirst();
        User user = pair.getSecond();

        giveEmail(user);

        giveChecker(userService::existsByEmail, false);

        when(user.isEnabled()).thenReturn(false);

        List<Tokens> tokens = giveTokens();
        when(user.getTokens()).thenReturn(tokens);

        for (Tokens token : tokens) {
            doNothing().when(token).updateExpiredAt(any(LocalDateTime.class));
        }

        giveUserId(user);

        ExceptionMessages exceptionMessage = USER_NOT_FOUND;
        doThrow(new IllegalArgumentException(exceptionMessage.getMessage()))
                .when(registrationService).sendToken(anyLong(), anyString(), anyString());

        // when/then
        String newEmail = testUpdateEmailThrowsIllegalArgumentException(principal, exceptionMessage);

        tokens.forEach(token -> verify(token).updateExpiredAt(any(LocalDateTime.class)));

        verifyUpdateEmailThrowsCustomValidException(user, newEmail, times(1));
        verify(user).isEnabled();
        verify(user, never()).updateEnabled();
        verify(user).getTokens();
        verify(registrationService).sendToken(eq(user.getId()), eq(newEmail), eq("update"));
        verify(user, never()).updateEmail(eq(newEmail));
    }

    @Test
    public void updateNicknameWithAnonymousUser() {
        testUpdateNicknameThrowsPrincipalException(null, UNAUTHORIZED);
    }

    @Test
    public void updateNicknameWithNonExistentUsername() {
        Principal principal = mock(Principal.class);
        testUpdateNicknameThrowsPrincipalException(principal, USERNAME_NOT_FOUND);
    }

    @Test
    public void updateNicknameWithInvalidNewNickname() {
        testUpdateNicknameThrowsCustomValidException(true, INVALID_NEW_NICKNAME);
    }

    @Test
    public void updateNicknameWithExistentNickname() {
        giveChecker(userService::existsByNickname, true);

        testUpdateNicknameThrowsCustomValidException(false, NICKNAME_EXISTS);
    }

    @Test
    public void deleteUserWithAnonymousUser() {
        testDeleteUserThrowsPrincipalException(null, UNAUTHORIZED);
    }

    @Test
    public void deleteUserWithNonExistentUsername() {
        Principal principal = mock(Principal.class);
        testDeleteUserThrowsPrincipalException(principal, USERNAME_NOT_FOUND);
    }

    @Test
    public void deleteUserThrowsBuildValidateAccessTokenUrlException() {
        testDeleteUserThrowsSocialException(SOCIAL);
    }

    @Test
    public void deleteUserThrowsGetRefreshTokenException() {
        testDeleteUserThrowsSocialException(REFRESH_TOKEN);
    }

    @Test
    public void deleteUserWithNonExistentUser() {
        // given
        Pair<Principal, User> pair = givePrincipal();
        Principal principal = pair.getFirst();
        User user = pair.getSecond();

        giveSocial(user);

        Cookie cookie = mock(Cookie.class);
        when(customOAuth2UserService.unlinkSocial(any(SocialType.class), anyString(), any(User.class)))
                .thenReturn(cookie);

        giveUserId(user);

        ExceptionMessages exceptionMessage = USER_NOT_FOUND;
        doThrow(new IllegalArgumentException(exceptionMessage.getMessage()))
                .when(userService).delete(anyLong());

        // when/then
        testDeleteUserThrowsIllegalArgumentException(principal, exceptionMessage);

        verifyDeleteUser(user, times(1));
    }

    @Test
    public void unlinkSocialWithAnonymousUser() {
        testUnlinkSocialThrowsPrincipalException(null, UNAUTHORIZED);
    }

    @Test
    public void unlinkSocialWithNonExistentUsername() {
        Principal principal = mock(Principal.class);
        testUnlinkSocialThrowsPrincipalException(principal, USERNAME_NOT_FOUND);
    }

    @Test
    public void unlinkSocialThrowsBuildValidateAccessTokenUrlException() {
        testUnlinkSocialThrowsSocialException(SOCIAL);
    }

    @Test
    public void unlinkSocialThrowsGetRefreshTokenException() {
        testUnlinkSocialThrowsSocialException(REFRESH_TOKEN);
    }

    private void testUpdatePasswordThrowsPrincipalException(Principal principal, ExceptionMessages exceptionMessage) {
        // given
        PasswordUpdateDto requestDto = createPasswordUpdateDto(null);

        throwIllegalArgumentException(principal, exceptionMessage);

        // when/then
        assertIllegalArgumentException(() -> userUpdateService.updatePassword(requestDto, principal), exceptionMessage);

        verifyUpdatePassword(principal, never(), requestDto.getOldPassword());
    }

    private void testUpdatePasswordThrowsCustomValidException(Fields field, boolean matches,
                                                              ExceptionMessages exceptionMessage) {
        // given
        PasswordUpdateDto requestDto = createPasswordUpdateDto(field);

        Pair<Principal, User> pair = givePrincipal();
        Principal principal = pair.getFirst();
        User user = pair.getSecond();

        when(user.getPassword()).thenReturn(PASSWORD_VALUE);

        when(encoder.matches(anyString(), anyString())).thenReturn(matches);

        String rejectedValue = getRejectedValue(field, requestDto);

        CustomValidException customValidException = createCustomValidException(field, rejectedValue, exceptionMessage);

        // when/then
        assertCustomValidException(() -> userUpdateService.updatePassword(requestDto, principal), customValidException);

        verifyUpdatePassword(principal, times(1), requestDto.getOldPassword());
    }

    private void testUpdateEmailThrowsPrincipalException(Principal principal, ExceptionMessages exceptionMessage) {
        // given
        throwIllegalArgumentException(principal, exceptionMessage);

        // when/then
        testUpdateEmailThrowsIllegalArgumentException(principal, exceptionMessage);
    }

    private String testUpdateEmailThrowsIllegalArgumentException(Principal principal, ExceptionMessages exceptionMessage) {
        // given
        EmailUpdateDto requestDto = createEmailUpdateDto(false);

        // when/then
        assertIllegalArgumentException(() -> userUpdateService.updateEmail(requestDto, principal), exceptionMessage);

        verifyPrincipal(principal);

        return requestDto.getEmail();
    }

    private void testUpdateEmailThrowsCustomValidException(boolean isInvalid, ExceptionMessages exceptionMessage) {
        // given
        EmailUpdateDto requestDto = createEmailUpdateDto(isInvalid);

        Pair<Principal, User> pair = givePrincipal();
        Principal principal = pair.getFirst();
        User user = pair.getSecond();

        giveEmail(user);

        String email = requestDto.getEmail();
        CustomValidException customValidException = createCustomValidException(
                EMAIL, requestDto.getEmail(), exceptionMessage);

        VerificationMode existMode = setExistMode(isInvalid);

        // when/then
        assertCustomValidException(() -> userUpdateService.updateEmail(requestDto, principal), customValidException);

        verifyPrincipal(principal);
        verifyUpdateEmailThrowsCustomValidException(user, email, existMode);
    }

    private void testUpdateNicknameThrowsPrincipalException(Principal principal, ExceptionMessages exceptionMessage) {
        // given
        NicknameUpdateDto requestDto = createNicknameUpdateDto(false);

        throwIllegalArgumentException(principal, exceptionMessage);

        // when/then
        assertIllegalArgumentException(() -> userUpdateService.updateNickname(requestDto, principal), exceptionMessage);

        verifyPrincipal(principal);
    }

    private void testUpdateNicknameThrowsCustomValidException(boolean isInvalid, ExceptionMessages exceptionMessage) {
        // given
        NicknameUpdateDto requestDto = createNicknameUpdateDto(isInvalid);

        Pair<Principal, User> pair = givePrincipal();
        Principal principal = pair.getFirst();
        User user = pair.getSecond();

        when(user.getNickname()).thenReturn(NICKNAME_VALUE);

        CustomValidException customValidException = createCustomValidException(
                NICKNAME, requestDto.getNickname(), exceptionMessage);

        VerificationMode existMode = setExistMode(isInvalid);

        // when/then
        assertCustomValidException(() -> userUpdateService.updateNickname(requestDto, principal), customValidException);

        verifyPrincipal(principal);
        verifyUpdateNickname(user, requestDto.getNickname(), existMode);
    }

    private void testDeleteUserThrowsPrincipalException(Principal principal, ExceptionMessages exceptionMessage) {
        // given
        throwIllegalArgumentException(principal, exceptionMessage);

        // when/then
        testDeleteUserThrowsIllegalArgumentException(principal, exceptionMessage);
    }

    private void testDeleteUserThrowsIllegalArgumentException(Principal principal, ExceptionMessages exceptionMessage) {
        assertIllegalArgumentException(() -> userUpdateService.deleteUser(principal, ACCESS_TOKEN), exceptionMessage);

        verifyPrincipal(principal);
    }

    private void testDeleteUserThrowsSocialException(Fields field) {
        // given
        Pair<Principal, User> pair = givePrincipal();
        Principal principal = pair.getFirst();
        User user = pair.getSecond();

        giveSocial(user);

        giveUsername(user);

        SocialException socialException = throwSocialException(user.getUsername(), field);

        // when/then
        assertSocialException(() -> userUpdateService.deleteUser(principal, ACCESS_TOKEN), socialException);

        verifyPrincipal(principal);
        verifyDeleteUser(user, never());
    }

    private void testUnlinkSocialThrowsPrincipalException(Principal principal, ExceptionMessages exceptionMessage) {
        // given
        throwIllegalArgumentException(principal, exceptionMessage);

        // when/then
        assertIllegalArgumentException(
                () -> userUpdateService.unlinkSocial(principal, SOCIAL_TYPE.getKey(), ACCESS_TOKEN), exceptionMessage);

        verifyUnlinkSocial(principal, never(), null);
    }

    private void testUnlinkSocialThrowsSocialException(Fields field) {
        // given
        Pair<Principal, User> pair = givePrincipal();
        Principal principal = pair.getFirst();
        User user = pair.getSecond();

        giveUsername(user);

        SocialException socialException = throwSocialException(user.getUsername(), field);

        // when/then
        assertSocialException(
                () -> userUpdateService.unlinkSocial(principal, SOCIAL_TYPE.getKey(), ACCESS_TOKEN), socialException);

        verifyUnlinkSocial(principal, times(1), user);
    }

    private PasswordUpdateDto createPasswordUpdateDto(Fields field) {
        String oldPassword = PASSWORD_VALUE;
        String password = oldPassword + ID;
        String confirmPassword = password;

        if (field != null) {
            switch (field) {
                case OLD_PASSWORD -> oldPassword = password;
                case PASSWORD -> password = oldPassword;
                case CONFIRM_PASSWORD -> confirmPassword = oldPassword;
            }
        }

        return PasswordUpdateDto.builder()
                .oldPassword(oldPassword).password(password).confirmPassword(confirmPassword).build();
    }

    private String getRejectedValue(Fields field, PasswordUpdateDto passwordUpdateDto) {
        if (OLD_PASSWORD.equals(field)) {
            return passwordUpdateDto.getOldPassword();
        } else if (PASSWORD.equals(field)) {
            return passwordUpdateDto.getPassword();
        }

        return passwordUpdateDto.getConfirmPassword();
    }

    private EmailUpdateDto createEmailUpdateDto(boolean isInvalid) {
        String email = isInvalid ? EMAIL_VALUE : EMAIL_PREFIX + ID + EMAIL_SUFFIX;

        return new EmailUpdateDto(email);
    }

    private NicknameUpdateDto createNicknameUpdateDto(boolean isInvalid) {
        String nickname = isInvalid ? NICKNAME_VALUE : NICKNAME_VALUE + ID;

        return new NicknameUpdateDto(nickname);
    }

    private Pair<Principal, User> givePrincipal() {
        return ServiceTestUtil.givePrincipal(principalHelper::getUserFromPrincipal);
    }

    private void giveEmail(User user) {
        ServiceTestUtil.giveEmail(user, EMAIL_VALUE);
    }

    private void giveChecker(Function<String, Boolean> checker, boolean exists) {
        ServiceTestUtil.giveChecker(checker, exists);
    }

    private List<Tokens> giveTokens() {
        List<Tokens> tokens = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            Tokens token = mock(Tokens.class);
            tokens.add(token);
        }

        return tokens;
    }

    private void giveSocial(User user) {
        when(user.getSocial()).thenReturn(SOCIAL_TYPE);
    }

    private void giveUsername(User user) {
        ServiceTestUtil.giveUsername(user, USERNAME.getKey() + ID);
    }

    private void giveUserId(User user) {
        when(user.getId()).thenReturn(ID);
    }

    private void throwIllegalArgumentException(Principal principal, ExceptionMessages exceptionMessage) {
        ServiceTestUtil.throwIllegalArgumentException(
                principalHelper::getUserFromPrincipal, principal, true, exceptionMessage);
    }

    private SocialException throwSocialException(String username, Fields field) {
        SocialException socialException = createSocialException(username, field);

        when(customOAuth2UserService.unlinkSocial(any(SocialType.class), anyString(), any(User.class)))
                .thenThrow(socialException);

        return socialException;
    }

    private CustomValidException createCustomValidException(Fields field, String rejectedValue,
                                                            ExceptionMessages exceptionMessage) {
        return ServiceTestUtil.createCustomValidException(field, rejectedValue, exceptionMessage);
    }

    private SocialException createSocialException(String username, Fields field) {
        return ServiceTestUtil.createSocialException(username, field, SOCIAL_TYPE);
    }

    private void assertIllegalArgumentException(Executable executable, ExceptionMessages exceptionMessage) {
        ServiceTestUtil.assertIllegalArgumentException(executable, exceptionMessage);
    }

    private void assertCustomValidException(Executable executable, CustomValidException customValidException) {
        ServiceTestUtil.assertCustomValidException(executable, customValidException);
    }

    private void assertSocialException(Executable executable, SocialException socialException) {
        ServiceTestUtil.assertSocialException(executable, socialException);
    }

    private VerificationMode setExistMode(boolean isInvalid) {
        return isInvalid ? never() : times(1);
    }

    private void verifyUpdatePassword(Principal principal, VerificationMode matchMode, String oldPassword) {
        verifyPrincipal(principal);
        verify(encoder, matchMode).matches(eq(oldPassword), eq(PASSWORD_VALUE));
        verify(registrationService, never()).encryptPassword(anyString());
    }

    private void verifyPrincipal(Principal principal) {
        verify(principalHelper).getUserFromPrincipal(principal, true);
    }

    private void verifyUpdateEmailThrowsCustomValidException(User user, String email, VerificationMode existMode) {
        verify(user).getEmail();
        verify(userService, existMode).existsByEmail(eq(email));
    }

    private void verifyUpdateNickname(User user, String nickname, VerificationMode existMode) {
        verify(user).getNickname();
        verify(userService, existMode).existsByNickname(eq(nickname));
        verify(user, never()).updateNickname(eq(nickname));
    }

    private void verifyDeleteUser(User user, VerificationMode deleteMode) {
        verify(user, times(2)).getSocial();
        verify(customOAuth2UserService).unlinkSocial(eq(user.getSocial()), eq(ACCESS_TOKEN), eq(user));
        verify(userService, deleteMode).delete(eq(user.getId()));
    }

    private void verifyUnlinkSocial(Principal principal, VerificationMode unlinkSocialMode, User user) {
        verifyPrincipal(principal);
        verify(customOAuth2UserService, unlinkSocialMode).unlinkSocial(eq(SOCIAL_TYPE), eq(ACCESS_TOKEN), eq(user));
    }
}
