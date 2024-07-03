package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.USERNAME_NOT_FOUND;
import static com.eskgus.nammunity.domain.enums.Fields.USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceExceptionTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    public void loadUserByUsernameWithNonExistentUsername() {
        // given
        String username = USERNAME.getKey();

        // when/then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername(username));

        assertEquals(USERNAME_NOT_FOUND.getMessage(), exception.getMessage());

        verify(userRepository).findByUsername(eq(username));
    }
}
