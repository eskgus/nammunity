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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfirmationApiControllerTest {
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

    @BeforeEach
    public void setUp() {
        Long userId = testDataHelper.signUp(1L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, userId);

        Long tokenId = testDataHelper.saveTokens(user);
        this.token = assertOptionalAndGetEntity(tokensRepository::findById, tokenId);
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    public void confirmToken() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get("/api/users/confirm");
        ResultMatcher resultMatcher = flash().attributeCount(0);

        mockMvcTestHelper.requestAndAssertStatusIsFound(requestBuilder, token.getToken(), resultMatcher);
    }

    @Transactional
    @Test
    @WithMockUser(username = "username1")
    public void checkUserEnabled() throws Exception {
        confirmEmail();

        // 일반 1. 회원가입
        requestAndAssertCheckUserEnabled("/users/sign-up/" + user.getId(), "/users/sign-in");

        // 일반 2. 이메일 변경
        String userInfo = "/users/my-page/update/user-info";
        requestAndAssertCheckUserEnabled(userInfo, userInfo);
    }

    private void confirmEmail() {
        testDataHelper.confirmTokens(token);
        assertThat(token.getConfirmedAt()).isNotNull();
        assertThat(token.getUser().isEnabled()).isTrue();
    }

    private void requestAndAssertCheckUserEnabled(String referer, String expectedContent) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get("/api/users/{id}/confirm", user.getId());
        ResultMatcher resultMatcher = mockMvcTestHelper.createResultMatcher(expectedContent);

        mockMvcTestHelper.requestAndAssertStatusIsOkWithReferer(requestBuilder, referer, resultMatcher);
    }

    @Test
    public void resendToken() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post("/api/users/confirm");
        Long requestDto = user.getId();

        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, requestDto);
    }
}
