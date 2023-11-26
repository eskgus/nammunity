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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfirmationApiControllerTest {
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
    public void confirmToken() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1 이메일 인증 토큰 저장
        // 3. user1의 token 찾기
        List<Tokens> result = tokensRepository.findByUser(user1);
        Assertions.assertThat(result.size()).isGreaterThan(0);
        Tokens token = result.get(0);

        // 4. "/api/users/confirm"으로 parameter token=token 담아서 get 요청
        // 5. 상태가 302 found인지 확인
        MvcResult mvcResult = mockMvc.perform(get("/api/users/confirm")
                        .param("token", token.getToken()))
                .andExpect(status().isFound())
                .andReturn();

        // 5. 응답 flash attribute에 "error" 없는지 확인
        Assertions.assertThat(mvcResult.getFlashMap().containsKey("error")).isFalse();

        // 6. token confirmedAt이 null이 아닌지 확인
        Assertions.assertThat(token.getConfirmedAt()).isNotNull();

        // 7. user enabled true인지 확인
        Assertions.assertThat(user1.isEnabled()).isTrue();
    }

    @Transactional
    @Test
    @WithMockUser(username = "username1")
    public void checkUserEnabled() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();
        Long userId = user1.getId();

        // 2. user1 이메일 인증 토큰 저장
        Tokens token = tokensRepository.findById(1L).get();
        Assertions.assertThat(tokensRepository.count()).isOne();

        // 3. user1 이메일 인증
        testDB.confirmTokens(token);
        Assertions.assertThat(token.getConfirmedAt()).isNotNull();
        Assertions.assertThat(token.getUser().isEnabled()).isTrue();

        String referer = "http://localhost:80/users/";

        // 일반 1. 회원가입
        requestAndAssertToCheckUserEnabled(userId, referer + "users/sign-up/" + userId, "sign-in");

        // 일반 2. 이메일 변경
        requestAndAssertToCheckUserEnabled(userId, referer + "my-page/update/user-info", "my-page");
    }

    @Test
    public void resendToken() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();
        Long userId = user1.getId();

        // 2. user1 이메일 인증 토큰 저장
        Assertions.assertThat(tokensRepository.count()).isOne();

        // 3. user id로 request map 생성
        Map<String, Long> request = new HashMap<>();
        request.put("id", userId);

        // 4. "/api/users/confirm"으로 request map 담아서 post 요청
        MvcResult mvcResult = mockMvc.perform(post("/api/users/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        // 5. 응답으로 "OK" 왔나 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("OK");

        // 6. token confirmedAt null인지 확인
        List<Tokens> result = tokensRepository.findByUser(user1);
        int numOfTokens = result.size();
        Assertions.assertThat(numOfTokens).isEqualTo(2);
        Assertions.assertThat(result.get(numOfTokens - 1).getConfirmedAt()).isNull();

        // 7. user enabled false인지 확인
        Assertions.assertThat(user1.isEnabled()).isFalse();
    }

    private void requestAndAssertToCheckUserEnabled(Long userId, String referer, String responseValue) throws Exception {
        // 1. "/api/users/confirm/{id}"의 pathVariable=userId, header referer=referer로 해서 get 요청
        MvcResult mvcResult = mockMvc.perform(get("/api/users/confirm/{id}", userId)
                        .header("referer", referer))
                .andExpect(status().isOk())
                .andReturn();

        // 2. 응답으로 "OK" 왔나 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("OK");

        // 3. "OK"의 값이 responseValue인지 확인
        Assertions.assertThat((String) map.get("OK")).contains(responseValue);
    }
}
