package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private static final Long ID = 1L;
    private static final String USERNAME = Fields.USERNAME.getKey() + ID;
    private static final String PASSWORD = Fields.PASSWORD.getKey() + ID;
    private static final String NICKNAME = Fields.NICKNAME.getKey() + ID;
    private static final String EMAIL = Fields.EMAIL.getKey() + ID + "@naver.com";

    @Test
    public void saveUsers() {
        // given
        RegistrationDto registrationDto = createRegistrationDto();

        User user = ServiceTestUtil.giveUserId(ID);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        Long result = userService.save(registrationDto);

        // then
        assertEquals(ID, result);

        verify(userRepository).save(any(User.class));
    }

    @Test
    public void findUsersByUsername() {
        // given
        User user = giveContentFinder(userRepository::findByUsername, String.class);

        // when
        User result = userService.findByUsername(USERNAME);

        // then
        assertEquals(user, result);

        verify(userRepository).findByUsername(eq(USERNAME));
    }

    @Test
    public void findUsersByEmail() {
        // given
        User user = giveContentFinder(userRepository::findByEmail, String.class);

        // when
        User result = userService.findByEmail(EMAIL);

        // then
        assertEquals(user, result);

        verify(userRepository).findByEmail(eq(EMAIL));
    }

    @Test
    public void deleteUsers() {
        // given
        User user = giveContentFinder(userRepository::findById, Long.class);

        doNothing().when(userRepository).delete(any(User.class));

        // when
        userService.delete(ID);

        // then
        verify(userRepository).findById(eq(ID));
        verify(userRepository).delete(eq(user));
    }

    @Test
    public void findUsersById() {
        // given
        User user = giveContentFinder(userRepository::findById, Long.class);

        // when
        User result = userService.findById(ID);

        // then
        assertEquals(user, result);

        verify(userRepository).findById(eq(ID));
    }

    @Test
    public void existsByUsername() {
        testExistsByField(userRepository::existsByUsername, userService::existsByUsername, USERNAME);

        verify(userRepository).existsByUsername(eq(USERNAME));
    }

    @Test
    public void existsByNickname() {
        testExistsByField(userRepository::existsByNickname, userService::existsByNickname, NICKNAME);

        verify(userRepository).existsByNickname(eq(NICKNAME));
    }

    @Test
    public void existsByEmail() {
        testExistsByField(userRepository::existsByEmail, userService::existsByEmail, EMAIL);

        verify(userRepository).existsByEmail(eq(EMAIL));
    }

    @Test
    public void searchUsersByNickname() {
        // given
        String keywords = "keyword";
        int page = 1;
        int size = 3;

        Page<UsersListDto> usersPage = ServiceTestUtil.giveContentsPage(userRepository::searchByNickname, String.class);

        // when
        Page<UsersListDto> result = userService.searchByNickname(keywords, page, size);

        // then
        assertEquals(usersPage, result);

        verify(userRepository).searchByNickname(eq(keywords), any(Pageable.class));
    }

    private void testExistsByField(Function<String, Boolean> checker, Function<String, Boolean> serviceChecker,
                                   String value) {
        // given
        giveChecker(checker);

        // when
        boolean result = serviceChecker.apply(value);

        // then
        assertTrue(result);
    }

    private RegistrationDto createRegistrationDto() {
        return RegistrationDto.builder()
                .username(USERNAME).password(PASSWORD).confirmPassword(PASSWORD)
                .nickname(NICKNAME).email(EMAIL).build();
    }

    private <ParamType> User giveContentFinder(Function<ParamType, Optional<User>> finder,
                                               Class<ParamType> paramType) {
        User user = mock(User.class);
        ServiceTestUtil.giveContentFinder(finder, paramType, user);

        return user;
    }

    private void giveChecker(Function<String, Boolean> checker) {
        ServiceTestUtil.giveChecker(checker, true);
    }
}
