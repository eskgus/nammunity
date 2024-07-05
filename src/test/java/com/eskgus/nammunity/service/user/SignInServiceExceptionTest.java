package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.email.EmailService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;

import java.util.function.Function;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static com.eskgus.nammunity.domain.enums.Fields.EMAIL;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SignInServiceExceptionTest {
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

    @Test
    public void increaseAttemptWithNonExistentUsername() {
        // given
        ExceptionMessages exceptionMessage = USERNAME_NOT_FOUND;
        throwIllegalArgumentException(userService::findByUsername, exceptionMessage);

        // when/then
        assertIllegalArgumentException(() -> signInService.increaseAttempt(USERNAME), exceptionMessage);

        verify(userService).findByUsername(eq(USERNAME));
    }

    @Test
    public void findUsernameWithNonExistentEmail() {
        // given
        String email = EMAIL.getKey() + "@naver.com";

        ExceptionMessages exceptionMessage = EMAIL_NOT_FOUND;
        throwIllegalArgumentException(userService::findByEmail, exceptionMessage);

        // when/then
        assertIllegalArgumentException(() -> signInService.findUsername(email), exceptionMessage);

        verify(userService).findByEmail(eq(email));
    }

    @Test
    public void findPasswordWithNonExistentUsername() {
        // given
        ExceptionMessages exceptionMessage = USERNAME_NOT_FOUND;
        throwIllegalArgumentException(userService::findByUsername, exceptionMessage);

        // when/then
        testFindPasswordException(exceptionMessage, never());
    }

    @Test
    public void findPasswordWithBannedUser() {
        // given
        ServiceTestUtil.giveUser(userService::findByUsername, String.class);

        // when/then
        testFindPasswordException(BANNED, times(1));
    }

    private void testFindPasswordException(ExceptionMessages exceptionMessage, VerificationMode bannedMode) {
        assertIllegalArgumentException(() -> signInService.findPassword(USERNAME), exceptionMessage);

        verify(userService).findByUsername(eq(USERNAME));
        verify(bannedUsersService, bannedMode).isAccountNonBanned(eq(USERNAME));
        verify(emailService, never()).setRandomPasswordEmailText(anyString());
        verify(emailService, never()).send(anyString(), anyString());
        verify(userUpdateService, never()).encryptAndUpdatePassword(any(User.class), anyString());
    }

    private <ReturnType> void throwIllegalArgumentException(Function<String, ReturnType> finder,
                                                            ExceptionMessages exceptionMessage) {
        ServiceTestUtil.throwIllegalArgumentException(finder, exceptionMessage);
    }

    private void assertIllegalArgumentException(Executable executable, ExceptionMessages exceptionMessage) {
        ServiceTestUtil.assertIllegalArgumentException(executable, exceptionMessage);
    }
}
