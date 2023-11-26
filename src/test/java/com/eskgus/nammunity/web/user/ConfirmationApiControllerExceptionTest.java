package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.tokens.TokensRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfirmationApiControllerExceptionTest {
    @Autowired
    private TestDB testDB;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokensRepository tokensRepository;

    @BeforeEach
    public void setUp() {
        this.mockMvc = testDB.setUp();

        // 1. user1 회원가입
        User user1 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();
        Assertions.assertThat(userRepository.count()).isOne();

        // 2. user1 이메일 인증 토큰 저장
        tokensRepository.findById(testDB.saveTokens(user1)).get();
        Assertions.assertThat(tokensRepository.count()).isOne();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Transactional
    @Test
    public void causeExceptionsOnConfirmingToken() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1 이메일 인증 토큰 저장
        // 예외 1. 인증 링크 존재 x
        requestAndAssertForExceptionOnConfirmingToken("abcde", "인증 링크가 존재하지");

        // 예외 2. 인증 링크 만료
        // 1-1. token expiredAt 만료 시키기
        user1.getTokens().forEach(token -> token.updateExpiredAt(LocalDateTime.now()));

        // 1-2. 만료된 token으로 인증 요청
        Tokens token = tokensRepository.findByUser(user1).get(0);
        requestAndAssertForExceptionOnConfirmingToken(token.getToken(), "만료");

        // 1-3. token confirmedAt이 null, user1의 enabled가 false인지 확인
        Assertions.assertThat(token.getConfirmedAt()).isNull();
        Assertions.assertThat(user1.isEnabled()).isFalse();

        // 예외 3. 이미 인증된 메일
        // 1-1. token confirmedAt 업데이트 + user enabled 업데이트해서 인증 완료시키기
        token.updateConfirmedAt(LocalDateTime.now());
        user1.updateEnabled();

        // 1-2. 이미 인증된 token으로 인증 요청
        requestAndAssertForExceptionOnConfirmingToken(token.getToken(), "이미");

        // 1-3. token confirmedAt이 not null, user1의 enabled가 true인지 확인
        Assertions.assertThat(token.getConfirmedAt()).isNotNull();
        Assertions.assertThat(user1.isEnabled()).isTrue();
    }

    @Test
    public void causeExceptionsOnCheckingUserEnabled() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1 이메일 인증 토큰 저장
        // 예외 1. 인증되지 않은 메일
        // 1-1. "/api/users/confirm/{id}"의 pathVariable=user1의 id로 해서 get 요청
        MvcResult mvcResult = mockMvc.perform(get("/api/users/confirm/{id}", user1.getId()))
                .andExpect(status().isOk())
                .andReturn();

        // 1-2. 응답으로 "error" 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");

        // 1-3. "error"의 값이 "인증되지 않은"인지 확인
        Assertions.assertThat((String) map.get("error")).contains("인증되지 않은");
    }

    @Transactional
    @Test
    @WithMockUser(username = "username1")
    public void causeExceptionsOnResendingToken() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1 이메일 인증 토큰 저장
        // 예외 1. 더 이상 재발송 x
        // 1-1. user createdDate 현재 시각 - 13분으로 업데이트해서 가입 시간 종료
        user1.updateCreatedDate(LocalDateTime.now().minusMinutes(13));

        // 1-2. 인증 메일 재발송 요청
        requestAndAssertForExceptionOnResendingToken(user1.getId(), "더 이상");

        // 5. db에서 user랑 tokens 삭제됐는지 확인 (가입 시간 지나서)
        Assertions.assertThat(tokensRepository.count()).isZero();
        Assertions.assertThat(userRepository.count()).isZero();

        // 예외 2. 이미 인증된 메일
        // 1-1. user2 회원가입
        User user2 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();
        Assertions.assertThat(userRepository.count()).isOne();

        // 1-2. user2 이메일 인증 토큰 저장
        Tokens token = tokensRepository.findById(testDB.saveTokens(user2)).get();
        Assertions.assertThat(tokensRepository.count()).isOne();

        // 1-3. user2 이메일 인증
        testDB.confirmTokens(token);
        Assertions.assertThat(token.getConfirmedAt()).isNotNull();
        Assertions.assertThat(token.getUser().isEnabled()).isTrue();

        // 1-4. 인증 메일 재발송 요청
         requestAndAssertForExceptionOnResendingToken(token.getUser().getId(), "이미");

         // 1-5. user2의 이메일 인증 토큰 개수가 1, 토큰의 confirmedAt이 not null, enabled가 true인지 확인
        List<Tokens> tokens = tokensRepository.findByUser(user2);
        Assertions.assertThat(tokens.size()).isOne();
        Assertions.assertThat(tokens.get(0).getConfirmedAt()).isNotNull();
        Assertions.assertThat(tokens.get(0).getUser().isEnabled()).isTrue();
    }

    private void requestAndAssertForExceptionOnConfirmingToken(String token, String responseValue) throws Exception {
        // 1. "/api/users/confirm"으로 parameter token=token 담아서 get 요청
        // 2. 상태가 302 found인지 확인
        MvcResult mvcResult = mockMvc.perform(get("/api/users/confirm")
                        .param("token", token))
                .andExpect(status().isFound())
                .andReturn();

        // 3. 응답으로 "error" 왔는지 확인
        Assertions.assertThat(mvcResult.getFlashMap().containsKey("error")).isTrue();

        // 4. "error"의 값이 responseValue인지 확인
        Assertions.assertThat((String) mvcResult.getFlashMap().get("error")).contains(responseValue);
    }

    private void requestAndAssertForExceptionOnResendingToken(Long userId, String responseValue) throws Exception {
        // 1. user id로 request map 생성
        Map<String, Long> request = new HashMap<>();
        request.put("id", userId);

        // 2. "/api/users/confirm"으로 request map 담아서 post 요청
        MvcResult mvcResult = mockMvc.perform(post("/api/users/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 "error" 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");

        // 4. "error"의 값이 responseValue인지 확인
        Assertions.assertThat((String) map.get("error")).contains(responseValue);
    }
}
