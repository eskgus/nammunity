package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.tokens.TokensRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfirmationApiControllerExceptionTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokensRepository tokensRepository;

    private User user;

    @BeforeEach
    public void setUp() {
        Long userId = testDB.signUp(1L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, userId);

        Long tokenId = testDB.saveTokens(user);
        assertOptionalAndGetEntity(tokensRepository::findById, tokenId);
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDB.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void confirmTokenExceptions() throws Exception {
        // 예외 1. 인증 링크 존재 x
        requestAndAssertConfirmTokenExceptions("인증 링크가 존재하지 않습니다.", "token");

        // 예외 2. 인증 링크 만료
        confirmTokenWithExpiredToken();

        // 예외 3. 이미 인증된 메일
        confirmTokenWithConfirmedToken();
    }

    private void requestAndAssertConfirmTokenExceptions(String expectedErrorMessage, String token) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get("/api/users/confirm");
        ResultMatcher resultMatcher = flash().attribute("error", expectedErrorMessage);

        mockMvcTestHelper.requestAndAssertStatusIsFound(requestBuilder, token, resultMatcher);
    }

    private void confirmTokenWithExpiredToken() throws Exception {
        Tokens token = saveToken();
        updateTokenExpiredAt(token);

        requestAndAssertConfirmTokenExceptions("인증 링크가 만료됐습니다.", token.getToken());
    }

    private Tokens saveToken() {
        Long tokenId = testDB.saveTokens(user);
        return assertOptionalAndGetEntity(tokensRepository::findById, tokenId);
    }

    private void updateTokenExpiredAt(Tokens token) {
        token.updateExpiredAt(LocalDateTime.now());
        tokensRepository.save(token);
    }

    private void confirmTokenWithConfirmedToken() throws Exception {
        Tokens token = saveToken();
        updateTokenConfirmedAt(token);

        requestAndAssertConfirmTokenExceptions("이미 인증된 이메일입니다.", token.getToken());
    }

    private void updateTokenConfirmedAt(Tokens token) {
        token.updateConfirmedAt(LocalDateTime.now());
        tokensRepository.save(token);
    }

    @Test
    public void checkUserEnabledExceptions() throws Exception {
        // 예외 1. 인증되지 않은 메일
        requestAndAssertCheckUserEnabledExceptions("인증되지 않은 이메일입니다.");

        // 예외 2. 사용자 존재 x
        userRepository.delete(user);
        requestAndAssertCheckUserEnabledExceptions("존재하지 않는 회원입니다.");
    }

    private void requestAndAssertCheckUserEnabledExceptions(String expectedContent) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get("/api/users/{id}/confirm", user.getId());
        ResultMatcher resultMatcher = mockMvcTestHelper.createResultMatcher(expectedContent);

        mockMvcTestHelper.requestAndAssertStatusIsBadRequestWithReferer(requestBuilder,
                "/users/sign-up/" + user.getId(), resultMatcher);
    }

    @Test
    public void resendTokenExceptions() throws Exception {
        // 예외 1. 더 이상 재발송 x
        resendTokenWithInvalidUserCreatedDate();

        // 예외 2. 이미 인증된 메일
        resendTokenWithEnabledUser();

        // 예외 3. 사용자 존재 x
        requestAndAssertResendTokenExceptions(user.getId(), "존재하지 않는 회원입니다.");
    }

    private void resendTokenWithInvalidUserCreatedDate() throws Exception {
        updateUserCreatedDate();

        requestAndAssertResendTokenExceptions(user.getId(), "더 이상 재발송할 수 없어요. 다시 가입해 주세요.");
    }

    private void updateUserCreatedDate() {
        user.updateCreatedDate(LocalDateTime.now().minusMinutes(13));
        userRepository.save(user);
    }

    private void requestAndAssertResendTokenExceptions(Long requestDto, String expectedContent) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post("/api/users/confirm");
        ResultMatcher resultMatcher = mockMvcTestHelper.createResultMatcher(expectedContent);

        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatcher);
    }

    private void resendTokenWithEnabledUser() throws Exception {
        User user = saveUserAndUpdateEnabled();

        requestAndAssertResendTokenExceptions(user.getId(), "이미 인증된 이메일입니다.");
    }

    private User saveUserAndUpdateEnabled() {
        Long user2Id = testDB.signUp(user.getId() + 1, Role.USER);
        User user2 = assertOptionalAndGetEntity(userRepository::findById, user2Id);

        updateUserEnabled(user2);

        return user2;
    }

    private void updateUserEnabled(User user) {
        user.updateEnabled();
        userRepository.save(user);
    }
}
