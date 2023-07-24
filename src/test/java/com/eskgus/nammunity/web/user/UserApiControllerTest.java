package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.domain.tokens.TokensRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.service.user.RegistrationService;
import com.eskgus.nammunity.web.dto.user.EmailUpdateDto;
import com.eskgus.nammunity.web.dto.user.NicknameUpdateDto;
import com.eskgus.nammunity.web.dto.user.PasswordUpdateDto;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Log4j2
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserApiControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private TokensRepository tokensRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegistrationService registrationService;

    @Test
    @Order(1)
    public void check() {
        // 1. "/api/users?username=username111"로 get 요청
        String url = "http://localhost:" + port + "/api/users?username=username111";
        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity(url, String.class);

        // 2. 응답으로 "OK" 왔나 확인
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isEqualTo("OK");
    }

    @Test
    @Order(2)
    public void signUp() {
        // 1. username, password, confirmPassword, nickname, email로 RegistrationDto 생성
        String username = "username111";
        String password = "password111";
        RegistrationDto requestDto = RegistrationDto.builder()
                .username(username).password(password).confirmPassword(password)
                .nickname("nickname1").email("email111@naver.com").build();

        // 2. "/api/users"로 registrationDto 담아서 post 요청
        String url = "http://localhost:" + port + "/api/users";
        ResponseEntity<Map> responseEntity = testRestTemplate.postForEntity(url, requestDto, Map.class);

        // 3. 응답으로 "OK" 왔나 확인
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).containsKey("OK");

        // 4. db에 저장됐나 확인
        Long id = Long.valueOf((String) responseEntity.getBody().get("OK"));
        Optional<User> result = userRepository.findById(id);
        Assertions.assertThat(result).isPresent();

        // 5. 저장된 user password 암호화됐나 확인
        User user = result.get();
        Assertions.assertThat(encoder.matches(password, user.getPassword())).isTrue();
    }

    @Test
    @Order(3)
    @WithMockUser(username = "username111", password = "password111")
    public void signInUser() throws Exception {
        // 1. 회원가입하고 user enabled 업데이트
        String token = tokensRepository.findByUser(userRepository.findById(1L).get()).get(0).getToken();
        registrationService.confirmToken(token);
        User user = userRepository.findById(1L).get();
        Assertions.assertThat(user.isEnabled()).isTrue();

        // 2. username, password 담아서 "/users/sign-in"으로 post 요청
        String url = "http://localhost:" + port + "/users/sign-in?username=" + user.getUsername()
                + "&password=password111";
        ResponseEntity<Map> responseEntity = testRestTemplate.postForEntity(url, null, Map.class);

        // 3. redirect uri가 "/"이랑 같은지 확인
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        URI redirectUri = responseEntity.getHeaders().getLocation();
        URI expectUri = URI.create("http://localhost:" + port + "/");
        Assertions.assertThat(redirectUri).isEqualTo(expectUri);

        // 4. signInUser() 응답으로 redirect된 화면에 user nickname 있나 확인
        MvcResult mvcResult = mockMvc.perform(get(redirectUri))
                .andExpect(status().isOk())
                .andReturn();
        Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains(user.getNickname());
    }

    @Test
    @Order(4)
    @WithMockUser(username = "username111", password = "password111")
    public void updatePassword() throws Exception {
        // 1. 회원가입 + 로그인 후
        // 2. oldPassword, password, confirmPassword로 PasswordUpdateDto 생성
        String password = "password222";
        PasswordUpdateDto requestDto = PasswordUpdateDto.builder()
                .oldPassword("password111").password(password).confirmPassword(password).build();

        // 3. "/api/users/update/password"로 put 요청
        MvcResult mvcResult = mockMvc.perform(put("/api/users/update/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 4. 응답으로 "OK" 왔나 확인
        Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains("OK");

        // 5. 비밀번호 password로 바뀌었나 확인
        User user = userRepository.findById(1L).get();
        Assertions.assertThat(encoder.matches(password, user.getPassword())).isTrue();
    }

    @Test
    @Order(5)
    @WithMockUser(username = "username111", password = "password222")
    public void updateNickname() throws Exception {
        // 1. 회원가입 + 로그인 후
        // 2. nickname으로 NicknameUpdateDto 생성
        String nickname = "nickname2";
        NicknameUpdateDto requestDto = new NicknameUpdateDto(nickname);

        // 3. "/api/users/update/nickname"으로 put 요청
        MvcResult mvcResult = mockMvc.perform(put("/api/users/update/nickname")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 4. 응답으로 "OK" 왔나 확인
        Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains("OK");

        // 5. 닉네임 nickname으로 바뀌었나 확인
        User user = userRepository.findById(1L).get();
        Assertions.assertThat(user.getNickname()).isEqualTo(nickname);
    }

    @Test
    @Order(6)
    @WithMockUser(username = "username111", password = "password222")
    public void updateEmail() throws Exception {
        // 1. 회원가입 + 로그인 후
        // 2. email로 EmailUpdateDto 생성
        String email = "email222@naver.com";
        EmailUpdateDto requestDto = new EmailUpdateDto(email);

        // 3. "/api/users/update/email"로 put 요청
        MvcResult mvcResult = mockMvc.perform(put("/api/users/update/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 4. 응답으로 "OK" 왔나 확인
        Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains("OK");

        // 5. 이메일 email로 바뀌었나 확인
        User user = userRepository.findById(1L).get();
        Assertions.assertThat(user.getEmail()).isEqualTo(email);
    }

    @Test
    @Order(7)
    @WithMockUser(username = "username111", password = "password222")
    public void deleteUser() throws Exception {
        // 1. 회원가입 + 로그인 후
        // 2. "/api/users/delete"로 delete 요청
        MvcResult mvcResult = mockMvc.perform(delete("/api/users/delete"))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 "OK" 왔나 확인
        Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains("OK");

        // 4. db에서 지워졌나 확인
        Assertions.assertThat(userRepository.count()).isZero();
    }

    public Map<String, Object> parseResponseJSON(String response) {
        Gson gson = new Gson();
        return gson.fromJson(response, Map.class);
    }
}
