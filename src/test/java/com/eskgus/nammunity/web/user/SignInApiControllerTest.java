package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Log4j2
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SignInApiControllerTest extends UserApiControllerTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        signUp();
    }

    @Test
    public void findUsername() throws Exception {
        // 1. 회원가입 후
        User user = userRepository.findById(1L).get();

        // 2. 가입한 email로 "/api/users/sign-in"으로 get 요청
        MvcResult mvcResult = mockMvc.perform(get("/api/users/sign-in")
                        .param("email", "email111@naver.com"))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 "OK" 왔나 확인
        Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains("OK");
    }

    @Test
    public void findPassword() throws Exception {
        // 1. 회원가입 후
        User user = userRepository.findById(1L).get();

        // 2. 가입한 username으로 "/api/users/sign-in"으로 put 요청
        MvcResult mvcResult = mockMvc.perform(put("/api/users/sign-in")
                        .param("username", user.getUsername()))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 "OK" 왔나 확인
        Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains("OK");

        // 4. request attribute에 "prePage" 있나 확인
        Object prePage = mvcResult.getRequest().getSession().getAttribute("prePage");
        Assertions.assertThat(prePage).isNotNull();

        // 5. "prePage"에 "password" 있나 확인
        Assertions.assertThat((String) prePage).contains("password");

        // 6. user createdDate랑 modifiedDate랑 다른지 확인
        Assertions.assertThat(user.getCreatedDate().isEqual(user.getModifiedDate())).isTrue();
    }
}
