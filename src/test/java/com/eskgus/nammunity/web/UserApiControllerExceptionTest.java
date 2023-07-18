package com.eskgus.nammunity.web;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.user.EmailUpdateDto;
import com.eskgus.nammunity.web.dto.user.NicknameUpdateDto;
import com.eskgus.nammunity.web.dto.user.PasswordUpdateDto;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Log4j2
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserApiControllerExceptionTest extends UserApiControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup(){
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .build();

        signUp();
        confirmToken();
    }

    @Test
    public void causeExceptionsInSignUp() {
        // 예외 1. username/password/nickname/email 유효성 검사 탈락
        RegistrationDto requestDto1 = RegistrationDto.builder()
                .username("").password("password").confirmPassword("password")
                .nickname("nickname!").email("email").build();
        String url = "http://localhost:" + port + "/api/users";
        ResponseEntity<Map> responseEntity1 = testRestTemplate.postForEntity(url, requestDto1, Map.class);
        Assertions.assertThat(responseEntity1.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity1.getBody()).containsKeys("username", "password", "nickname", "email");
        Optional<User> result = userRepository.findById(2L);
        Assertions.assertThat(result).isNotPresent();

        // 예외 2. username 중복
        RegistrationDto requestDto2 = RegistrationDto.builder()
                .username("username111").password("password111").confirmPassword("password111")
                .nickname("nickname2").email("email222@naver.com").build();
        ResponseEntity<Map> responseEntity2 = testRestTemplate.postForEntity(url, requestDto2, Map.class);
        Assertions.assertThat(responseEntity2.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity2.getBody()).containsKey("username");
        List<User> result2 = userRepository.findAll();
        Assertions.assertThat(result2.size()).isEqualTo(1);
    }

    @Test
    public void causeExceptionsInCheck() {
        // 예외 1. username 입력 x
        String url = "http://localhost:" + port + "/api/users?username=";
        ResponseEntity<String> responseEntity1 = testRestTemplate.getForEntity(url, String.class);
        Assertions.assertThat(responseEntity1.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity1.getBody()).contains("ID를 입력");

        // 예외 2. username 중복
        ResponseEntity<String> responseEntity2 = testRestTemplate.getForEntity(url + "username111", String.class);
        Assertions.assertThat(responseEntity2.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity2.getBody()).contains("이미 사용 중인 ID");
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void causeExceptionsInUpdatingPassword() throws Exception {
        // 예외 1. password 유효성 검사 탈락
        PasswordUpdateDto requestDto1 = PasswordUpdateDto.builder()
                .oldPassword("password111").password("password").confirmPassword("password").build();
        MvcResult mvcResult1 = mockMvc.perform(put("/api/users/update/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(requestDto1)))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> map = parseResponseJSON(mvcResult1.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("password");
        Assertions.assertThat((String) map.get("password")).contains("비밀번호는");

        // 예외 2. oldPassword랑 현재 password랑 일치 x
        PasswordUpdateDto requestDto2 = PasswordUpdateDto.builder()
                .oldPassword("password").password("password222").confirmPassword("password222").build();
        MvcResult mvcResult2 = mockMvc.perform(put("/api/users/update/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto2)))
                .andExpect(status().isOk())
                .andReturn();
        map = parseResponseJSON(mvcResult2.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("oldPassword");
        Assertions.assertThat((String) map.get("oldPassword")).contains("현재 비밀번호가 일치하지");

        User user = userRepository.findById(1L).get();
        Assertions.assertThat(encoder.matches("password111", user.getPassword())).isTrue();
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void causeExceptionsInUpdatingNickname() throws Exception{
        String nickname = "nickname2";
        RegistrationDto registrationDto = RegistrationDto.builder()
                .username("username222").password("password222").confirmPassword("password222")
                .nickname(nickname).email("email222@naver.com").build();
        String url = "http://localhost:" + port + "/api/users";
        ResponseEntity<Map> responseEntity = testRestTemplate.postForEntity(url, registrationDto, Map.class);
        Assertions.assertThat(responseEntity.getBody()).containsKey("OK");

        // 예외 1. nickname 유효성 검사 탈락
        NicknameUpdateDto requestDto1 = new NicknameUpdateDto("nickname111");
        MvcResult mvcResult1 = mockMvc.perform(put("/api/users/update/nickname")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(requestDto1)))
                .andExpect(status().isOk())
                .andReturn();
        Assertions.assertThat(mvcResult1.getResponse().getContentAsString()).contains("nickname");

        // 예외 2. nickname 중복
        NicknameUpdateDto requestDto2 = new NicknameUpdateDto(nickname);
        MvcResult mvcResult2 = mockMvc.perform(put("/api/users/update/nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto2)))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> map = parseResponseJSON(mvcResult2.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("이미");

        User user1 = userRepository.findById(1L).get();
        Assertions.assertThat(user1.getNickname()).isEqualTo("nickname1");
        User user2 = userRepository.findById(2L).get();
        Assertions.assertThat(user2.getNickname()).isEqualTo(nickname);
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void causeExceptionsInUpdatingEmail() throws Exception {
        String email = "email222@naver.com";
        RegistrationDto registrationDto = RegistrationDto.builder()
                .username("username222").password("password222").confirmPassword("password222")
                .nickname("nickname2").email(email).build();
        String url = "http://localhost:" + port + "/api/users";
        ResponseEntity<Map> responseEntity = testRestTemplate.postForEntity(url, registrationDto, Map.class);
        Assertions.assertThat(responseEntity.getBody()).containsKey("OK");

        // 예외 1. email 유효성 검사 탈락
        EmailUpdateDto requestDto1 = new EmailUpdateDto("email");
        MvcResult mvcResult1 = mockMvc.perform(put("/api/users/update/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto1)))
                .andExpect(status().isOk())
                .andReturn();
        Assertions.assertThat(mvcResult1.getResponse().getContentAsString()).contains("email");

        // 예외 2. email 중복
        EmailUpdateDto requestDto2 = new EmailUpdateDto(email);
        MvcResult mvcResult2 = mockMvc.perform(put("/api/users/update/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto2)))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> map = parseResponseJSON(mvcResult2.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("이미");

        User user1 = userRepository.findById(1L).get();
        Assertions.assertThat(user1.getEmail()).contains("email111");
        User user2 = userRepository.findById(2L).get();
        Assertions.assertThat(user2.getEmail()).isEqualTo(email);
    }
}
