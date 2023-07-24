package com.eskgus.nammunity.web.user;

import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Log4j2
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SignInApiControllerExceptionTest extends SignInApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .build();
    }

    @Test
    public void causeExceptionsInFindingUsername() throws Exception {
        // 예외 1. 이메일 입력 x
        MvcResult mvcResult1 = mockMvc.perform(get("/api/users/sign-in")
                        .param("email", ""))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> map = parseResponseJSON(mvcResult1.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("입력");

        // 예외 2. 이메일 형식 x
        MvcResult mvcResult2 = mockMvc.perform(get("/api/users/sign-in")
                        .param("email", "email"))
                .andExpect(status().isOk())
                .andReturn();
        map = parseResponseJSON(mvcResult2.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("형식");

        // 예외 3. 가입되지 x 이메일
        MvcResult mvcResult3 = mockMvc.perform(get("/api/users/sign-in")
                        .param("email", "email111@naver.com"))
                .andExpect(status().isOk())
                .andReturn();
        map = parseResponseJSON(mvcResult3.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("가입되지");
    }

    @Test
    public void causeExceptionsInFindingPassword() throws Exception {
        // 예외 1. username 입력 x
        MvcResult mvcResult1 = mockMvc.perform(put("/api/users/sign-in")
                        .param("username", ""))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> map = parseResponseJSON(mvcResult1.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("입력");

        // 예외 2. 존재하지 x username
        MvcResult mvcResult2 = mockMvc.perform(put("/api/users/sign-in")
                        .param("username", "username111"))
                .andExpect(status().isOk())
                .andReturn();
        map = parseResponseJSON(mvcResult2.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("존재하지");
    }
}
