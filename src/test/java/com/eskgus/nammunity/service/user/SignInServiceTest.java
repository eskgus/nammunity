package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.email.EmailService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SignInServiceTest {
    @Mock
    private UserService userService;

    @Mock
    private BannedUsersService bannedUsersService;

    @Mock
    private UserUpdateService userUpdateService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SignInService signInService;

    private static final String USERNAME = Fields.USERNAME.getKey();
    private static final String EMAIL = Fields.EMAIL.getKey() + "@naver.com";

    @Test
    public void increaseAttemptOnly() {
        testIncreaseAttempt(1, never());
    }

    @Test
    public void increaseAttemptAndUpdateLocked() {
        testIncreaseAttempt(5, times(1));
    }

    @Test
    public void resetAttempt() {
        // given
        User user = mock(User.class);

        doNothing().when(user).resetAttempt();

        // when
        signInService.resetAttempt(user);

        // then
        verify(user).resetAttempt();
    }

    @Test
    public void findUsernameWithRegularRegistration() {
        testFindUsername(USERNAME, USERNAME.substring(0, 3));
    }

    @Test
    public void findUsernameWithSocialRegistration() {
        String username = "G_" + USERNAME;
        testFindUsername(username, username);
    }

    @Test
    public void findPasswordWithLockedUser() {
        testFindPassword(true);
    }

    @Test
    public void findPasswordWithUnlockedUser() {
        testFindPassword(false);
    }

    private void testIncreaseAttempt(int attempt, VerificationMode lockedMode) {
        // given
        User user = giveUser(userService::findByUsername);

        when(user.increaseAttempt()).thenReturn(attempt);

        if (attempt == 5) {
            when(user.isLocked()).thenReturn(false);
            doNothing().when(user).updateLocked();
        }

        // when
        int result = signInService.increaseAttempt(USERNAME);

        // then
        assertEquals(attempt, result);

        verifyLocked(user, lockedMode, lockedMode);
        verify(user).increaseAttempt();
    }

    private void testFindUsername(String username, String expectedResult) {
        // given
        User user = giveUser(userService::findByEmail);

        ServiceTestUtil.giveUsername(user, username);

        // when
        String result = signInService.findUsername(EMAIL);

        // then
        assertTrue(result.contains(expectedResult));

        verify(userService).findByEmail(eq(EMAIL));
        verify(user).getUsername();
    }

    private void testFindPassword(boolean isLocked) {
        // given
        User user = giveUser(userService::findByUsername);

        when(bannedUsersService.isAccountNonBanned(anyString())).thenReturn(true);

        ServiceTestUtil.giveEmail(user, EMAIL);

        String text = "text";
        when(emailService.setRandomPasswordEmailText(anyString())).thenReturn(text);

        doNothing().when(emailService).send(anyString(), anyString());

        doNothing().when(userUpdateService).encryptAndUpdatePassword(any(User.class), anyString());

        when(user.isLocked()).thenReturn(isLocked);

        if (isLocked) {
            doNothing().when(user).updateLocked();
        }

        VerificationMode updateMode = isLocked ? times(1) : never();

        // when
        signInService.findPassword(USERNAME);

        // then
        verifyLocked(user, times(1), updateMode);
        verify(bannedUsersService).isAccountNonBanned(eq(USERNAME));
        verify(user).getEmail();
        verify(emailService).setRandomPasswordEmailText(anyString());
        verify(emailService).send(eq(EMAIL), eq(text));
        verify(userUpdateService).encryptAndUpdatePassword(eq(user), anyString());
    }

    private User giveUser(Function<String, User> finder) {
        return ServiceTestUtil.giveUser(finder, String.class);
    }

    private void verifyLocked(User user, VerificationMode lockedMode, VerificationMode updateMode) {
        verify(userService).findByUsername(eq(USERNAME));
        verify(user, lockedMode).isLocked();
        verify(user, updateMode).updateLocked();
    }
}
