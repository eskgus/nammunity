package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SignInApiControllerTest {
    @Autowired
    private TestDB testDB;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        this.mockMvc = testDB.setUp();

        // 1. user1 회원가입
        testDB.signUp(1L, Role.USER);
        Assertions.assertThat(userRepository.count()).isOne();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void findUsername() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. 가입한 email로 "/api/users/sign-in"으로 get 요청
        MvcResult mvcResult = mockMvc.perform(get("/api/users/sign-in")
                        .param("email", user1.getEmail()))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 "OK" 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("OK");

        // 4. "OK"의 값이 username 앞 3글자인지 확인
        Assertions.assertThat((String) map.get("OK")).contains(user1.getUsername().substring(0, 3));
    }

    @Test
    public void findPassword() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. 가입한 username으로 "/api/users/sign-in"으로 put 요청
        MvcResult mvcResult = mockMvc.perform(put("/api/users/sign-in")
                        .param("username", user1.getUsername()))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 "OK" 왔나 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("OK");

        // 4. request attribute에 "prePage" 있나 확인
        Object prePage = mvcResult.getRequest().getSession().getAttribute("prePage");
        Assertions.assertThat(prePage).isNotNull();

        // 5. "prePage"에 "password" 있나 확인
        Assertions.assertThat((String) prePage).contains("password");

        // 6. user createdDate랑 modifiedDate랑 다른지 확인
        Assertions.assertThat(user1.getCreatedDate().isEqual(user1.getModifiedDate())).isTrue();
    }
}
