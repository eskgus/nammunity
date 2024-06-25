package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
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
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfirmationApiControllerExceptionIntegrationTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokensRepository tokensRepository;

    private User user;
    private Tokens token;

    private static final String REQUEST_MAPPING = "/api/users";

    @BeforeEach
    public void setUp() {
        Long userId = testDataHelper.signUp(1L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, userId);

        Long tokenId = testDataHelper.saveTokens(user);
        this.token = assertOptionalAndGetEntity(tokensRepository::findById, tokenId);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    @WithAnonymousUser
    public void confirmTokenWithNonExistentToken() throws Exception {
        // given
        // when/then
        testConfirmTokenException(TOKEN_NOT_FOUND, "token");
    }

    @Test
    @WithAnonymousUser
    public void confirmTokenWithConfirmedToken() throws Exception {
        // given
        updateTokenConfirmedAt();

        // when/then
        testConfirmTokenException(EMAIL_CONFIRMED, token.getToken());
    }

    @Test
    @WithAnonymousUser
    public void confirmTokenWithExpiredToken() throws Exception {
        // given
        updateTokenExpiredAt();

        // when/then
        testConfirmTokenException(TOKEN_EXPIRED, token.getToken());
    }

    @Test
    @WithAnonymousUser
    public void checkUserEnabledWithNonExistentUserId() throws Exception {
        testCheckUserEnabledException(user.getId() + 1, USER_NOT_FOUND);
    }

    @Test
    @WithAnonymousUser
    public void checkUserEnabledWithNotConfirmedEmail() throws Exception {
        testCheckUserEnabledException(user.getId(), EMAIL_NOT_CONFIRMED);
    }

    @Test
    @WithAnonymousUser
    public void resendTokenWithNonExistentUserId() throws Exception {
        // given
        // when/then
        testResendTokenException(USER_NOT_FOUND, user.getId() + 1);
    }

    @Test
    @WithAnonymousUser
    public void resendTokenWithConfirmedEmail() throws Exception {
        // given
        updateUserEnabled();

        // when/then
        testResendTokenException(EMAIL_CONFIRMED, user.getId());
    }

    @Test
    @WithAnonymousUser
    public void resendTokenThrowsResendNotAllowedIllegalArgumentException() throws Exception {
        // given
        updateUserCreatedDate();

        // when/then
        testResendTokenException(RESEND_NOT_ALLOWED, user.getId());
    }

    private void updateTokenConfirmedAt() {
        token.updateConfirmedAt(LocalDateTime.now());
        tokensRepository.save(token);
    }

    private void updateTokenExpiredAt() {
        token.updateExpiredAt(LocalDateTime.now().minusMinutes(1));
        tokensRepository.save(token);
    }

    private void updateUserEnabled() {
        user.updateEnabled();
        userRepository.save(user);
    }

    private void updateUserCreatedDate() {
        user.updateCreatedDate(LocalDateTime.now().minusMinutes(13));
        userRepository.save(user);
    }

    private void testConfirmTokenException(ExceptionMessages exceptionMessage, String token) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(REQUEST_MAPPING + "/confirm");
        ResultMatcher resultMatcher = flash().attribute("error", exceptionMessage.getMessage());
        mockMvcTestHelper.performAndExpectFound(requestBuilder, token, resultMatcher);
    }

    private void testCheckUserEnabledException(Long userId, ExceptionMessages exceptionMessage) throws Exception {
        // given
        // when/then
        MockHttpServletRequestBuilder requestBuilder = get(REQUEST_MAPPING + "/{id}/confirm", userId);
        ResultMatcher resultMatcher = createResultMatcher(exceptionMessage);
        mockMvcTestHelper.performAndExpectBadRequestWithReferer(requestBuilder, "referer", resultMatcher);
    }

    private void testResendTokenException(ExceptionMessages exceptionMessage, Long requestDto) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING + "/confirm");
        ResultMatcher resultMatcher = createResultMatcher(exceptionMessage);
        mockMvcTestHelper.performAndExpectBadRequest(requestBuilder, requestDto, resultMatcher);
    }

    private ResultMatcher createResultMatcher(ExceptionMessages exceptionMessage) {
        return mockMvcTestHelper.createResultMatcher(exceptionMessage);
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }
}
