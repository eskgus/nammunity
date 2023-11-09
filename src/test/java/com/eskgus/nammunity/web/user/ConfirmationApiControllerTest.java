package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.tokens.TokensRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfirmationApiControllerTest extends UserApiControllerTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokensRepository tokensRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup(){
        signUp();
    }

    @Test
    public void confirmToken() throws Exception {
        // 1. 회원가입 후
        User user = userRepository.findAll().get(0);
        Long id = user.getId();

        // 2. tokensRepository에서 user로 token 찾기
        List<Tokens> result = tokensRepository.findByUser(user);
        Assertions.assertThat(result.size()).isGreaterThan(0);
        Tokens tokens = result.get(0);

        // 3. token 담아서 "/api/users/confirm"으로 get 요청
        MvcResult mvcResult = mockMvc.perform(get("/api/users/confirm")
                        .param("token", tokens.getToken()))
                .andExpect(status().isFound())
                .andReturn();

        // 4. 응답 flash attribute에 "error" 없는지 확인
        Assertions.assertThat(mvcResult.getFlashMap().containsKey("error")).isFalse();

        // 5. token confirmed_at이 null이 아닌지 확인
        user = userRepository.findById(id).get();
        tokens = tokensRepository.findByUser(user).get(0);
        Assertions.assertThat(tokens.getConfirmedAt()).isNotNull();

        // 6. user enabled true인지 확인
        Assertions.assertThat(user.isEnabled()).isTrue();
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void checkUserEnabled() throws Exception {
        // 일반 1. 회원가입
        // 1. 회원가입 + 이메일 인증 후
        confirmToken();

        // 2. header referer에 회원가입 주소 넣어서 "/api/users/confirm/{id}"로 get 요청
        MvcResult mvcResult1 = mockMvc.perform(get("/api/users/confirm/{id}", 1)
                        .header("referer", "http://localhost:80/users/sign-up/1"))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 "OK" 왔나 확인
        Map<String, Object> map = parseResponseJSON(mvcResult1.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("OK");

        // 4. "OK"에 "sign-in" 있나 확인
        Assertions.assertThat((String) map.get("OK")).contains("sign-in");

        // 일반 2. 이메일 변경
        // 1. 회원가입 + 이메일 인증 후
        // 2. header referer에 마이 페이지 주소 넣어서 "/api/users/confirm/{id}"로 get 요청
        MvcResult mvcResult2 = mockMvc.perform(get("/api/users/confirm/{id}", 1)
                        .header("referer", "http://localhost:80/users/my-page/update/user-info"))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 "OK" 왔나 확인
        map = parseResponseJSON(mvcResult2.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("OK");

        // 4. "OK"에 "my-page" 있나 확인
        Assertions.assertThat((String) map.get("OK")).contains("my-page");
    }

    @Test
    public void resendToken() throws Exception {
        // 1. 회원가입 후
        // 2. Map에 "id": id 담아서 "/api/users/confirm"으로 post 요청
        Map<String, Long> request = new HashMap<>();
        request.put("id", 1L);
        MvcResult mvcResult = mockMvc.perform(post("/api/users/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 "OK" 왔나 확인
        Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains("OK");

        // 4. token confirmedAt null인지 확인
        User user = userRepository.findById(1L).get();
        List<Tokens> result = tokensRepository.findByUser(user);
        Assertions.assertThat(result.size()).isGreaterThan(1);
        Assertions.assertThat(result.get(1).getConfirmedAt()).isNull();

        // 5. user enabled false인지 확인
        Assertions.assertThat(user.isEnabled()).isFalse();
    }
}
