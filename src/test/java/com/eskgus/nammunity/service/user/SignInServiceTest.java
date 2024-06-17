package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.email.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;

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

    @Test
    public void findUsername() {
        // given
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("username");
        when(user.getEmail()).thenReturn("email@naver.com");
        when(userService.findByEmail(anyString())).thenReturn(user);

        // when
        String result = signInService.findUsername(user.getEmail());

        // then
        assertTrue(result.contains(user.getUsername().substring(0, 3)));

        verify(userService).findByEmail(eq(user.getEmail()));
    }

    @Test
    public void findUsernameWithNonExistentEmail() {
        // given
        when(userService.findByEmail(anyString())).thenThrow(IllegalArgumentException.class);

        String email = "email@naver.com";

        // when/then
        assertThrows(IllegalArgumentException.class, () -> signInService.findUsername(email));

        verify(userService).findByEmail(eq(email));
    }

    @Test
    public void findPassword() {
        // given
        User user = giveUser();

        when(bannedUsersService.isAccountNonBanned(anyString())).thenReturn(true);

        when(user.getEmail()).thenReturn("email@naver.com");
        when(emailService.setRandomPasswordEmailText(anyString())).thenReturn("text");

        when(user.isLocked()).thenReturn(true);

        // when
        signInService.findPassword(user.getUsername());

        // then
        verify(userService).findByUsername(eq(user.getUsername()));
        verify(bannedUsersService).isAccountNonBanned(eq(user.getUsername()));
        verify(emailService).setRandomPasswordEmailText(anyString());
        verify(emailService).send(eq(user.getEmail()), anyString());
        verify(userUpdateService).encryptAndUpdatePassword(eq(user), anyString());
        verify(user).isLocked();
        verify(user).updateLocked();
    }

    @Test
    public void findPasswordWithNonExistentUsername() {
        // given
        User user = createMockedUser();
        when(userService.findByUsername(anyString())).thenThrow(IllegalArgumentException.class);

        // when/then
        assertThrowsAndVerifyFindPassword(user, never());
    }

    @Test
    public void findPasswordWithNonExistentBannedUser() {
        // given
        User user = giveUser();

        when(bannedUsersService.isAccountNonBanned(anyString())).thenThrow(IllegalArgumentException.class);

        // when/then
        assertThrowsAndVerifyFindPassword(user, times(1));
    }

    @Test
    public void findPasswordWithBannedUser() {
        // given
        User user = giveUser();

        when(bannedUsersService.isAccountNonBanned(anyString())).thenReturn(false);

        // when/then
        String exceptionMessage = assertThrowsAndVerifyFindPassword(user, times(1));
        assertEquals("활동 정지된 계정입니다. 자세한 내용은 메일을 확인하세요.", exceptionMessage);
    }

    private User giveUser() {
        User user = createMockedUser();
        when(userService.findByUsername(anyString())).thenReturn(user);

        return user;
    }

    private User createMockedUser() {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("username");

        return user;
    }

    private String assertThrowsAndVerifyFindPassword(User user, VerificationMode mode) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> signInService.findPassword(user.getUsername()));

        verify(userService).findByUsername(eq(user.getUsername()));
        verify(bannedUsersService, mode).isAccountNonBanned(eq(user.getUsername()));

        verify(emailService, never()).setRandomPasswordEmailText(anyString());
        verify(emailService, never()).send(anyString(), anyString());
        verify(userUpdateService, never()).encryptAndUpdatePassword(any(User.class), anyString());
        verify(user, never()).isLocked();
        verify(user, never()).updateLocked();

        return exception.getMessage();
    }
}
