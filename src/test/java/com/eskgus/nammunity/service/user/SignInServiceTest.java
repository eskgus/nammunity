package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.helper.TestDataHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class SignInServiceTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SignInService signInService;

    private User user;

    @BeforeEach
    public void setUp() {
        Long userId = testDataHelper.signUp(1L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, userId);
    }

    private User assertOptionalAndGetEntity(Function<Long, Optional<User>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    public void findUsername() {
        String username = signInService.findUsername(user.getEmail());
        assertFindUsername(username);
    }

    private void assertFindUsername(String username) {
        String expectedEncryptedUsername = user.getUsername().substring(0, 3);
        assertThat(username).contains(expectedEncryptedUsername);
    }

    @Transactional
    @Test
    public void findPassword() {
        String oldPassword = user.getPassword();

        signInService.findPassword(user.getUsername());
        assertFindPassword(oldPassword);
    }

    private void assertFindPassword(String oldPassword) {
        String newPassword = user.getPassword();
        assertThat(newPassword).isNotEqualTo(oldPassword);
    }
}
