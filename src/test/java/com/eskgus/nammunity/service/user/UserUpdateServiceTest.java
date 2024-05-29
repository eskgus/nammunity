package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.web.dto.user.EmailUpdateDto;
import com.eskgus.nammunity.web.dto.user.NicknameUpdateDto;
import com.eskgus.nammunity.web.dto.user.PasswordUpdateDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.Principal;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserUpdateServiceTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserUpdateService userUpdateService;

    @Autowired
    private BCryptPasswordEncoder encoder;

    private User user;

    @BeforeEach
    public void setUp() {
        Long userId = testDB.signUp(1L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, userId);
    }

    private User assertOptionalAndGetEntity(Function<Long, Optional<User>> finder, Long contentId) {
        return testDB.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void updatePassword() {
        PasswordUpdateDto passwordUpdateDto = createPasswordUpdateDto();
        Principal principal = user::getUsername;

        Long userId = userUpdateService.updatePassword(passwordUpdateDto, principal);

        assertUpdatePassword(userId, passwordUpdateDto.getPassword());
    }

    private PasswordUpdateDto createPasswordUpdateDto() {
        String newPassword = "password" + (user.getId() + 1);
        return PasswordUpdateDto.builder()
                .oldPassword("password" + user.getId()).password(newPassword).confirmPassword(newPassword).build();
    }

    private void assertUpdatePassword(Long userId, String newPassword) {
        User user = assertOptionalAndGetEntity(userRepository::findById, userId);
        assertThat(encoder.matches(newPassword, user.getPassword())).isTrue();
    }

    @Test
    public void updateNickname() {
        NicknameUpdateDto nicknameUpdateDto = createNicknameUpdateDto();
        Principal principal = user::getUsername;

        Long userId = userUpdateService.updateNickname(nicknameUpdateDto, principal);

        assertUpdateNickname(userId, nicknameUpdateDto.getNickname());
    }

    private NicknameUpdateDto createNicknameUpdateDto() {
        String newNickname = "nickname" + (user.getId() + 1);
        return new NicknameUpdateDto(newNickname);
    }

    private void assertUpdateNickname(Long userId, String newNickname) {
        User user = assertOptionalAndGetEntity(userRepository::findById, userId);
        assertThat(user.getNickname()).isEqualTo(newNickname);
    }

    @Test
    public void updateEmail() {
        EmailUpdateDto emailUpdateDto = createEmailUpdateDto();
        Principal principal = user::getUsername;

        Long userId = userUpdateService.updateEmail(emailUpdateDto, principal);

        assertUpdateEmail(userId, emailUpdateDto.getEmail());
    }

    private EmailUpdateDto createEmailUpdateDto() {
        String newEmail = "email" + (user.getId() + 1) + "@naver.com";
        return new EmailUpdateDto(newEmail);
    }

    private void assertUpdateEmail(Long userId, String newEmail) {
        User user = assertOptionalAndGetEntity(userRepository::findById, userId);
        assertThat(user.getEmail()).isEqualTo(newEmail);
    }

    @Test
    public void deleteUser() {
        Principal principal = user::getUsername;
        HttpHeaders actualHeaders = userUpdateService.deleteUser(principal, null);

        assertDeleteUser(actualHeaders);
    }

    private void assertDeleteUser(HttpHeaders actualHeaders) {
        Optional<User> result = userRepository.findById(user.getId());
        assertThat(result).isNotPresent();

        assertThat(actualHeaders).isNull();
    }
}
