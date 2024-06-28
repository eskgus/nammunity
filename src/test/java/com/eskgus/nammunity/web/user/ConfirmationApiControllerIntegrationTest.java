package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.tokens.Tokens;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Optional;
import java.util.function.Function;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfirmationApiControllerIntegrationTest {
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
    public void confirmToken() throws Exception {
        // given
        // when/then
        MockHttpServletRequestBuilder requestBuilder = get(REQUEST_MAPPING + "/confirm");
        ResultMatcher resultMatcher = flash().attributeCount(0);
        mockMvcTestHelper.performAndExpectFound(requestBuilder, token.getToken(), resultMatcher);
    }

    @Test
    @WithAnonymousUser
    public void checkUserEnabledInSignUp() throws Exception {
        String signUp = "/users/sign-up";
        String signIn = "/users/sign-in";

        testCheckUserEnabled(signUp, signIn);
    }

    @Test
    @WithMockUser(username = "username1")
    public void checkUserEnabledInMyPage() throws Exception {
        String myPage = "/users/my-page/update/user-info";
        testCheckUserEnabled(myPage, myPage);
    }

    @Test
    @WithAnonymousUser
    public void resendToken() throws Exception {
        // given
        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING + "/confirm");
        mockMvcTestHelper.performAndExpectOk(requestBuilder, user.getId());
    }

    private void testCheckUserEnabled(String referer, String redirectUrl) throws Exception {
        // given
        updateUserEnabled();

        // when/then
        MockHttpServletRequestBuilder requestBuilder = get(REQUEST_MAPPING + "/{id}/confirm", user.getId());
        ResultMatcher resultMatcher = mockMvcTestHelper.createResultMatcher(redirectUrl);
        mockMvcTestHelper.performAndExpectOkWithReferer(requestBuilder, referer, resultMatcher);
    }

    private void updateUserEnabled() {
        user.updateEnabled();
        userRepository.save(user);
    }

    private <Entity> Entity assertOptionalAndGetEntity(Function<Long, Optional<Entity>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }
}
