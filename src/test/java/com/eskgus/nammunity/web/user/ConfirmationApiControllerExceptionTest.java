package com.eskgus.nammunity.web.user;

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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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

    @Transactional
    @Test
    public void causeExceptionsOnConfirmingToken() throws Exception {
//        // 1. user1 회원가입
//        User user1 = userRepository.findById(1L).get();
//
//        // 2. user1 이메일 인증 토큰 저장
//        // 예외 1. 인증 링크 존재 x
//        requestAndAssertForExceptionOnConfirmingToken("abcde", "인증 링크가 존재하지");
//
//        // 예외 2. 인증 링크 만료
//        // 1-1. token expiredAt 만료 시키기
//        user1.getTokens().forEach(token -> token.updateExpiredAt(LocalDateTime.now()));
//
//        // 1-2. 만료된 token으로 인증 요청
//        Tokens token = tokensRepository.findByUser(user1).get(0);
//        requestAndAssertForExceptionOnConfirmingToken(token.getToken(), "만료");
//
//        // 1-3. token confirmedAt이 null, user1의 enabled가 false인지 확인
//        Assertions.assertThat(token.getConfirmedAt()).isNull();
//        Assertions.assertThat(user1.isEnabled()).isFalse();
//
//        // 예외 3. 이미 인증된 메일
//        // 1-1. token confirmedAt 업데이트 + user enabled 업데이트해서 인증 완료시키기
//        token.updateConfirmedAt(LocalDateTime.now());
//        user1.updateEnabled();
//
//        // 1-2. 이미 인증된 token으로 인증 요청
//        requestAndAssertForExceptionOnConfirmingToken(token.getToken(), "이미");
//
//        // 1-3. token confirmedAt이 not null, user1의 enabled가 true인지 확인
//        Assertions.assertThat(token.getConfirmedAt()).isNotNull();
//        Assertions.assertThat(user1.isEnabled()).isTrue();
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

//    private void requestAndAssertForExceptionOnConfirmingToken(String token, String responseValue) throws Exception {
//        // 1. "/api/users/confirm"으로 parameter token=token 담아서 get 요청
//        // 2. 상태가 302 found인지 확인
//        MvcResult mvcResult = mockMvc.perform(get("/api/users/confirm")
//                        .param("token", token))
//                .andExpect(status().isFound())
//                .andReturn();
//
//        // 3. 응답으로 "error" 왔는지 확인
//        Assertions.assertThat(mvcResult.getFlashMap().containsKey("error")).isTrue();
//
//        // 4. "error"의 값이 responseValue인지 확인
//        Assertions.assertThat((String) mvcResult.getFlashMap().get("error")).contains(responseValue);
//    }
}
