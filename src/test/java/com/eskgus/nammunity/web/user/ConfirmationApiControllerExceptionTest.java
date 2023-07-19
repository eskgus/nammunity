package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.tokens.TokensRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.service.tokens.TokensService;
import com.eskgus.nammunity.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Log4j2
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfirmationApiControllerExceptionTest extends ConfirmationApiControllerTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokensRepository tokensRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokensService tokensService;

    @Autowired
    private UserService userService;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup(){
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .build();

        signUp();
    }

    @Test
    public void causeExceptionsInConfirmingToken() throws Exception {
        User user = userRepository.findById(1L).get();

        // 예외 1. 인증 링크 존재 x
        String url = "/api/users/confirm";
        MvcResult mvcResult1 = mockMvc.perform(get(url).param("token", "abcde"))
                .andExpect(status().isFound())
                .andReturn();
        Assertions.assertThat(mvcResult1.getFlashMap().containsKey("error")).isTrue();
        Assertions.assertThat((String) mvcResult1.getFlashMap().get("error")).contains("인증 링크가 존재하지");

        // 예외 2. 인증 링크 만료
        tokensService.updateExpiredAtAllByUser(user, LocalDateTime.now());
        Tokens tokens = tokensRepository.findByUser(user).get(0);
        MvcResult mvcResult2 = mockMvc.perform(get(url).param("token", tokens.getToken()))
                .andExpect(status().isFound())
                .andReturn();
        Assertions.assertThat(mvcResult2.getFlashMap().containsKey("error")).isTrue();
        Assertions.assertThat((String) mvcResult2.getFlashMap().get("error")).contains("만료");
        Assertions.assertThat(tokens.getConfirmedAt()).isNull();
        Assertions.assertThat(user.isEnabled()).isFalse();

        // 예외 3. 이미 인증된 메일
        tokensService.updateConfirmedAt(tokens.getToken(), LocalDateTime.now());
        userService.updateEnabled(user);
        tokens = tokensRepository.findByUser(user).get(0);
        MvcResult mvcResult3 = mockMvc.perform(get(url).param("token", tokens.getToken()))
                .andExpect(status().isFound())
                .andReturn();
        Assertions.assertThat(mvcResult3.getFlashMap().containsKey("error")).isTrue();
        Assertions.assertThat((String) mvcResult3.getFlashMap().get("error")).contains("이미");
        Assertions.assertThat(tokens.getConfirmedAt()).isNotNull();
        Assertions.assertThat(user.isEnabled()).isTrue();
    }

    @Test
    public void causeExceptionsInCheckingUserEnabled() throws Exception {
        // 예외 1. 인증되지 않은 메일
        MvcResult mvcResult = mockMvc.perform(get("/api/users/confirm/{id}", 1))
                .andExpect(status().isOk())
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains("error");
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void causeExceptionsInResendingToken() throws Exception {
        // 예외 1. 더 이상 재발송 x
        userService.updateCreatedDate(1L, LocalDateTime.now().minusMinutes(13));
        Map<String, Long> request = new HashMap<>();
        request.put("id", 1L);
        MvcResult mvcResult1 = mockMvc.perform(post("/api/users/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> map = parseResponseJSON(mvcResult1.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("더 이상");
        Assertions.assertThat(tokensRepository.count()).isZero();
        Assertions.assertThat(userRepository.count()).isZero();

        // 예외 2. 이미 인증된 메일
        signUp();
        confirmToken();
        User user = userRepository.findById(2L).get();
        request.replace("id", user.getId());
        MvcResult mvcResult2 = mockMvc.perform(post("/api/users/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        map = parseResponseJSON(mvcResult2.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("이미");
        List<Tokens> tokens = tokensRepository.findByUser(user);
        Assertions.assertThat(tokens.size()).isOne();
        Assertions.assertThat(tokens.get(0).getConfirmedAt()).isNotNull();
        Assertions.assertThat(user.isEnabled()).isTrue();
    }
}
