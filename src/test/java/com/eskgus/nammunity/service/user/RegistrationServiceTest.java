package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.tokens.TokensRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class RegistrationServiceTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokensRepository tokensRepository;

    @Autowired
    private RegistrationService registrationService;

    private User user;

    @BeforeEach
    public void setUp() {
        Long userId = testDataHelper.signUp(1L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, userId);
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    public void checkUserEnabled() {
        updateUserEnabled(user);

        // 1. "/users/sign-in"
        callAndAssertCheckUserEnabled(user.getId(), "/users/sign-up");

        // 2. "/users/my-page/update/user-info"
        callAndAssertCheckUserEnabled(user.getId(), "/users/my-page/update/user-info");
    }

    private void updateUserEnabled(User user) {
        user.updateEnabled();
        userRepository.save(user);
        assertThat(user.isEnabled()).isTrue();
    }

    private void callAndAssertCheckUserEnabled(Long id, String referer) {
        String actualUrl = registrationService.checkUserEnabled(id, referer);
        String expectedUrl = getExpectedUrl(referer);
        assertCheckUserEnabled(actualUrl, expectedUrl);
    }

    private String getExpectedUrl(String referer) {
        if (referer.contains("/sign-up")) {
            return "/users/sign-in";
        }
        return "/users/my-page/update/user-info";
    }

    private void assertCheckUserEnabled(String actualUrl, String expectedUrl) {
        assertThat(actualUrl).isEqualTo(expectedUrl);
    }

    @Test
    public void resendToken() {
        saveToken(user);

        requestAndAssertResendToken(user);
    }

    private void saveToken(User user) {
        Long tokenId = testDataHelper.saveTokens(user);
        assertOptionalAndGetEntity(tokensRepository::findById, tokenId);
    }

    private void requestAndAssertResendToken(User user) {
        registrationService.resendToken(user.getId());

        assertTokens(user);
    }

    private void assertTokens(User user) {
        List<Tokens> tokens = tokensRepository.findByUser(user);
        assertThat(tokens.size()).isEqualTo(2);
        assertThat(tokens.get(0).getExpiredAt()).isBefore(LocalDateTime.now());
        assertThat(tokens.get(1).getExpiredAt()).isAfter(LocalDateTime.now());
    }

    @Test
    public void signUp() {
        long id = user.getId() + 1;
        RegistrationDto registrationDto = RegistrationDto.builder()
                .username("username" + id).password("password" + id).confirmPassword("password" + id)
                .nickname("nickname" + id).email("email" + id + "@naver.com").build();

        Long savedUserId = registrationService.signUp(registrationDto);

        assertOptionalAndGetEntity(userRepository::findById, savedUserId);
    }

    @Test
    public void check() {
        // 1. username
        requestAndAssertCheck("username2", null, null);

        // 2. nickname
        requestAndAssertCheck(null, "nickname2", null);

        // 3. email
        requestAndAssertCheck(null, null, "email2@naver.com");
    }

    private void requestAndAssertCheck(String username, String nickname, String email) {
        boolean result = registrationService.check(username, nickname, email);
        assertThat(result).isTrue();
    }
}
