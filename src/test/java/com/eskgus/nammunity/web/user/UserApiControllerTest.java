package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.tokens.TokensRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.user.EmailUpdateDto;
import com.eskgus.nammunity.web.dto.user.NicknameUpdateDto;
import com.eskgus.nammunity.web.dto.user.PasswordUpdateDto;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserApiControllerTest {
    @Autowired
    private TestDB testDB;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokensRepository tokensRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @BeforeEach
    public void setUp() {
        this.mockMvc = testDB.setUp();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void check() throws Exception {
        // 1. "/api/users"로 parameter username="username1" 담아서 get 요청
        MvcResult mvcResult = mockMvc.perform(get("/api/users")
                        .param("username", "username1"))
                .andExpect(status().isOk())
                .andReturn();

        // 2. 응답으로 "OK" 왔나 확인
        Assertions.assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("OK");
    }

    @Test
    public void signUp() throws Exception {
        // 1. username, password, confirmPassword, nickname, email로 RegistrationDto 생성
        String username = "username1";
        String password = "password1";
        RegistrationDto requestDto = RegistrationDto.builder()
                .username(username).password(password).confirmPassword(password)
                .nickname("nickname1").email("email1@naver.com").build();

        // 2. "/api/users"로 registrationDto 담아서 post 요청
        MvcResult mvcResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 "OK" 왔나 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("OK");

        // 4. "OK"의 값으로 user 찾고
        Long id = Long.valueOf((String) map.get("OK"));
        Optional<User> result = userRepository.findById(id);
        Assertions.assertThat(result).isPresent();

        // 5. db에 저장됐나 확인
        User user = result.get();
        Assertions.assertThat(user.getUsername()).isEqualTo(username);

        // 5. 저장된 user password 암호화됐나 확인
        Assertions.assertThat(encoder.matches(password, user.getPassword())).isTrue();
    }

    @Transactional
    @Test
    @WithMockUser(username = "username1", password = "password1")   // redirect 테스트할 때 쓰는 거
    public void signInUser() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();

        // 2. user1 enabled true로 업데이트 (이메일 인증한 셈)
        user1.updateEnabled();
        Assertions.assertThat(user1.isEnabled()).isTrue();

        // 3. "/users/sign-in"으로 parameter username=user1의 username, password=user1의 password 담아서 post 요청
        // 4. 응답 상태가 302 found인지 확인
        // 5. redirectedUrl이 "/"이랑 같은지 확인
        MvcResult mvcResult = mockMvc.perform(post("/users/sign-in")
                        .param("username", user1.getUsername())
                        .param("password", "password1"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        // 6. signInUser() 응답으로 redirect된 화면에 user1 nickname 있나 확인
        String redirectUrl = mvcResult.getResponse().getRedirectedUrl();
        MvcResult mvcResult2 = mockMvc.perform(get(redirectUrl))
                .andExpect(status().isOk())
                .andReturn();
        Assertions.assertThat(mvcResult2.getResponse().getContentAsString()).contains(user1.getNickname());
    }

    @Transactional
    @Test
    @WithMockUser(username = "username1")
    public void updatePassword() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();

        // 2. oldPassword, password, confirmPassword로 PasswordUpdateDto 생성해서 Object에 저장
        Long userId = user1.getId();
        String oldPassword = "password" + userId;
        String password = oldPassword + userId;

        Object requestDto = PasswordUpdateDto.builder()
                .oldPassword(oldPassword).password(password).confirmPassword(password).build();

        // 3. 비밀번호 변경 요청
        requestAndAssertToUpdateInfo(requestDto, "password");

        // 4. 비밀번호 password로 바뀌었나 확인
        Assertions.assertThat(encoder.matches(password, user1.getPassword())).isTrue();
    }

    @Transactional
    @Test
    @WithMockUser(username = "username1")
    public void updateNickname() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();

        // 2. nickname으로 NicknameUpdateDto 생성해서 Object에 저장
        String nickname = "nickname2";
        Object requestDto = new NicknameUpdateDto(nickname);

        // 3. 닉네임 변경 요청
        requestAndAssertToUpdateInfo(requestDto, "nickname");

        // 4. 닉네임 nickname으로 바뀌었나 확인
        Assertions.assertThat(user1.getNickname()).isEqualTo(nickname);
    }

    @Transactional
    @Test
    @WithMockUser(username = "username1")
    public void updateEmail() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();

        // 2. user1 이메일 인증 토큰 저장
        Tokens token = tokensRepository.findById(testDB.saveTokens(user1)).get();
        Assertions.assertThat(tokensRepository.count()).isOne();

        // 3. user1 이메일 인증
        testDB.confirmTokens(token);
        Assertions.assertThat(token.getConfirmedAt()).isNotNull();
        Assertions.assertThat(token.getUser().isEnabled()).isTrue();

        // 4. email로 EmailUpdateDto 생성해서 Object에 저장
        String email = "email2@naver.com";
        Object requestDto = new EmailUpdateDto(email);

        // 5. 이메일 변경 요청
        requestAndAssertToUpdateInfo(requestDto, "email");

        // 6. 이메일 email로 바뀌었나 확인
        User user = userRepository.findById(1L).get();
        Assertions.assertThat(user.getEmail()).isEqualTo(email);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteUser() throws Exception {
        // 1. user1 회원가입
        testDB.signUp(1L, Role.USER);

        // 2. "/api/users/delete"로 delete 요청
        MvcResult mvcResult = mockMvc.perform(delete("/api/users/delete"))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 "OK" 왔나 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("OK");

        // 4. db에서 지워졌나 확인
        Assertions.assertThat(userRepository.count()).isZero();
    }

    private void requestAndAssertToUpdateInfo(Object requestDto, String type) throws Exception {
        // 1. "/api/users/update/" + type으로 requestDto 담아서 put 요청
        MvcResult mvcResult = mockMvc.perform(put("/api/users/update/" + type)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 2. 응답으로 "OK" 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("OK");
    }
}
