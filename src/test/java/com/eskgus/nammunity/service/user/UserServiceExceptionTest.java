package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.util.ServiceTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static com.eskgus.nammunity.domain.enums.Fields.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceExceptionTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private static final Long ID = 1L;

    @Test
    public void findUsersByUsernameWithNonExistentUsername() {
        // given
        String username = USERNAME.getKey();

        // when/then
        assertIllegalArgumentException(() -> userService.findByUsername(username), USERNAME_NOT_FOUND);

        verify(userRepository).findByUsername(eq(username));
    }

    @Test
    public void findUsersByEmailWithNonExistentEmail() {
        // given
        String email = EMAIL.getKey() + "@naver.com";

        // when/then
        assertIllegalArgumentException(() -> userService.findByEmail(email), EMAIL_NOT_FOUND);

        verify(userRepository).findByEmail(eq(email));
    }

    @Test
    public void deleteUsersWithNonExistentUser() {
        // given
        ExceptionMessages exceptionMessage = USER_NOT_FOUND;
        ServiceTestUtil.throwIllegalArgumentException(userRepository::findById, exceptionMessage);

        // when/then
        assertIllegalArgumentException(() -> userService.delete(ID), exceptionMessage);

        verify(userRepository).findById(eq(ID));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    public void findUsersByIdWithNonExistentUser() {
        // given
        // when/then
        assertIllegalArgumentException(() -> userService.findById(ID), USER_NOT_FOUND);

        verify(userRepository).findById(eq(ID));
    }

    private void assertIllegalArgumentException(Executable executable, ExceptionMessages exceptionMessage) {
        ServiceTestUtil.assertIllegalArgumentException(executable, exceptionMessage);
    }
}
