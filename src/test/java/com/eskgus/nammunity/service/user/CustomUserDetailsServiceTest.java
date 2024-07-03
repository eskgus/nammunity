package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.util.ServiceTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static com.eskgus.nammunity.domain.enums.Fields.USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    public void loadUserByUsernameWithRoleAdmin() {
        testLoadUserByUsername(Role.ADMIN);
    }

    @Test
    public void loadUserByUsernameWithRoleUser() {
        testLoadUserByUsername(Role.USER);
    }

    private void testLoadUserByUsername(Role role) {
        // given
        String username = USERNAME.getKey();

        User user = mock(User.class);
        ServiceTestUtil.giveContentFinder(userRepository::findByUsername, String.class, user);
        ServiceTestUtil.giveUsername(user, username);

        when(user.getRole()).thenReturn(role);

        // when
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // then
        assertEquals(username, result.getUsername());
        assertEquals(role.getKey(), getAuthority(result));

        verify(userRepository).findByUsername(eq(username));
        verify(user).getRole();
    }

    private String getAuthority(UserDetails result) {
        return result.getAuthorities().iterator().next().getAuthority();
    }
}
