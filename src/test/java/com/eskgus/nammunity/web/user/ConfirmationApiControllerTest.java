package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.util.TestDB;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfirmationApiControllerTest {
    @Autowired
    private TestDB testDB;

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
        Long userId = testDB.signUp(1L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, userId);

        Long tokenId = testDB.saveTokens(user);
        this.token = assertOptionalAndGetEntity(tokensRepository::findById, tokenId);
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDB.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Transactional
    @Test
    public void confirmToken() throws Exception {
//        // 1. user1 회원가입
//        User user1 = userRepository.findById(1L).get();
//
//        // 2. user1 이메일 인증 토큰 저장
//        // 3. user1의 token 찾기
//        List<Tokens> result = tokensRepository.findByUser(user1);
//        Assertions.assertThat(result.size()).isGreaterThan(0);
//        Tokens token = result.get(0);
//
//        // 4. "/api/users/confirm"으로 parameter token=token 담아서 get 요청
//        // 5. 상태가 302 found인지 확인
//        MvcResult mvcResult = mockMvc.perform(get("/api/users/confirm")
//                        .param("token", token.getToken()))
//                .andExpect(status().isFound())
//                .andReturn();
//
//        // 5. 응답 flash attribute에 "error" 없는지 확인
//        Assertions.assertThat(mvcResult.getFlashMap().containsKey("error")).isFalse();
//
//        // 6. token confirmedAt이 null이 아닌지 확인
//        Assertions.assertThat(token.getConfirmedAt()).isNotNull();
//
//        // 7. user enabled true인지 확인
//        Assertions.assertThat(user1.isEnabled()).isTrue();
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
        testDB.confirmTokens(token);
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
