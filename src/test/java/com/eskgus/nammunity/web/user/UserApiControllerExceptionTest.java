package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.user.EmailUpdateDto;
import com.eskgus.nammunity.web.dto.user.NicknameUpdateDto;
import com.eskgus.nammunity.web.dto.user.PasswordUpdateDto;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
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

import java.util.Arrays;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserApiControllerExceptionTest {
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
    public void causeExceptionsOnSignUp() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 예외 1. username/password/nickname/email 유효성 검사 탈락
        // 1-1. 유효하지 않은 값들로 RegistrationDto 생성
        RegistrationDto inValidRequestDto = RegistrationDto.builder()
                .username("").password("password").confirmPassword("password")
                .nickname("nickname!").email("email").build();
        // 1-2. String 배열에 응답 키 값 저장
        String[] responseKeys = { "username", "password", "nickname", "email" };
        requestAndAssertForExceptionOnSignUp(inValidRequestDto, responseKeys);

        // 예외 2. username 중복
        // 1-1. 이미 가입된 username으로 RegistrationDto 생성
        RegistrationDto existentUsernameRequestDto = RegistrationDto.builder()
                .username(user1.getUsername()).password("password1").confirmPassword("password1")
                .nickname("nickname2").email("email222@naver.com").build();
        // 1-2. String 배열에서 "username" 빼고 삭제 (username이 중복인지 확인하는 거)
        responseKeys = Arrays.stream(responseKeys).filter(key -> key.equals("username")).toArray(String[]::new);
        requestAndAssertForExceptionOnSignUp(existentUsernameRequestDto, responseKeys);
    }

    @Test
    public void causeExceptionsOnCheck() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 예외 1. username 입력 x
        requestAndAssertForExceptionOnCheck("", "ID를 입력");

        // 예외 2. username 중복
        requestAndAssertForExceptionOnCheck(user1.getUsername(), "이미 사용 중인 ID");
    }

    @Test
    @WithMockUser(username = "username1")
    public void causeExceptionsOnUpdatingPassword() throws Exception {
        // 1. user1 회원가입
        userRepository.findById(1L).get();

        // 예외 1. password 유효성 검사 탈락
        // 1-1. PasswordUpdateDto 생성
        PasswordUpdateDto invalidPasswordRequestDto = PasswordUpdateDto.builder()
                .oldPassword("password1").password("password").confirmPassword("password").build();

        // 1-2. passwordUpdateDto로 UpdatingRequestDto 생성
        UpdatingRequestDto invalidPasswordDto = UpdatingRequestDto.builder()
                .type("password").requestDto(invalidPasswordRequestDto).responseKey("password").responseValue("비밀번호는").build();

        // 1-3. updatingRequestDto 담아서 요청
        requestAndAssertForExceptionOnUpdatingInfo(invalidPasswordDto);

        // 예외 2. oldPassword랑 현재 password랑 일치 x
        PasswordUpdateDto mismatchedPasswordRequestDto = PasswordUpdateDto.builder()
                .oldPassword("password2").password("password2").confirmPassword("password2").build();
        UpdatingRequestDto mismatchedPasswordDto = UpdatingRequestDto.builder()
                .type("password").requestDto(mismatchedPasswordRequestDto).responseKey("oldPassword").responseValue("현재 비밀번호가 일치하지").build();
        requestAndAssertForExceptionOnUpdatingInfo(mismatchedPasswordDto);
    }

    @Test
    @WithMockUser(username = "username1")
    public void causeExceptionsOnUpdatingNickname() throws Exception {
        // 1. user1 회원가입 + user2 회원가입
        User user2 = userRepository.findById(testDB.signUp(2L, Role.USER)).get();
        Assertions.assertThat(userRepository.count()).isEqualTo(2);

        // 예외 1. nickname 유효성 검사 탈락
        // 1-1. NicknameUpdateDto 생성
        NicknameUpdateDto invalidNicknameRequestDto = new NicknameUpdateDto("nickname111");

        // 1-2. nicknameUpdateDto로 UpdatingRequestDto 생성
        UpdatingRequestDto invalidNicknameDto = UpdatingRequestDto.builder()
                .type("nickname").requestDto(invalidNicknameRequestDto).responseKey("nickname").responseValue("닉네임은").build();

        // 1-3. updatingRequestDto 담아서 요청
        requestAndAssertForExceptionOnUpdatingInfo(invalidNicknameDto);

        // 예외 2. nickname 중복
        NicknameUpdateDto existentNicknameRequestDto = new NicknameUpdateDto(user2.getNickname());
        UpdatingRequestDto existentNicknameDto = UpdatingRequestDto.builder()
                .type("nickname").requestDto(existentNicknameRequestDto).responseKey("error").responseValue("이미").build();
        requestAndAssertForExceptionOnUpdatingInfo(existentNicknameDto);
    }

    @Transactional
    @Test
    @WithMockUser(username = "username1")
    public void causeExceptionsOnUpdatingEmail() throws Exception {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(testDB.signUp(2L, Role.USER)).get();
        Assertions.assertThat(userRepository.count()).isEqualTo(2);

        // 2. user1 enabled true로 업데이트 (이메일 바꿀 때 enabled false로 바꾸는데, 그전에 true인지 확인 먼저 해서 enabled를 true로 만들어줘야 함)
        user1.updateEnabled();
        Assertions.assertThat(user1.isEnabled()).isTrue();

        // 예외 1. email 유효성 검사 탈락
        // 1-1. EmailUpdateDto 생성
        EmailUpdateDto invalidEmailRequestDto = new EmailUpdateDto("email");

        // 1-2. emailUpdateDto로 UpdatingRequestDto 생성
        UpdatingRequestDto invalidEmailDto = UpdatingRequestDto.builder()
                .type("email").requestDto(invalidEmailRequestDto).responseKey("email").responseValue("이메일 형식").build();

        // 1-3. updatingRequestDto 담아서 요청
        requestAndAssertForExceptionOnUpdatingInfo(invalidEmailDto);

        // 예외 2. email 중복
        EmailUpdateDto existentEmailRequestDto = new EmailUpdateDto(user2.getEmail());
        UpdatingRequestDto existentEmailDto = UpdatingRequestDto.builder()
                .type("email").requestDto(existentEmailRequestDto).responseKey("error").responseValue("이미").build();
        requestAndAssertForExceptionOnUpdatingInfo(existentEmailDto);
    }

    @Getter
    private static class UpdatingRequestDto {
        private String type;
        private String responseKey;
        private String responseValue;
        private Object requestDto;

        @Builder
        public UpdatingRequestDto(String type, String responseKey, String responseValue, Object requestDto) {
            this.type = type;
            this.responseKey = responseKey;
            this.responseValue = responseValue;
            this.requestDto = requestDto;
        }
    }

    private void requestAndAssertForExceptionOnSignUp(RegistrationDto requestDto, String[] responseKeys) throws Exception {
        // 1. "/api/users"로 registrationDto 담아서 post 요청
        MvcResult mvcResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 2. 응답으로 responseKeys 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKeys(responseKeys);

        // 3. db에 저장된 사용자 수 1인지 확인
        Assertions.assertThat(userRepository.count()).isOne();
    }

    private void requestAndAssertForExceptionOnCheck(String username, String responseValue) throws Exception {
        // 1. "/api/users"로 paramter username=username 담아서 get 요청
        MvcResult mvcResult = mockMvc.perform(get("/api/users")
                        .param("username", username))
                .andExpect(status().isOk())
                .andReturn();

        // 2. 응답으로 responseValue 왔는지 확인
        Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains(responseValue);
    }

    private void requestAndAssertForExceptionOnUpdatingInfo(UpdatingRequestDto updatingRequestDto) throws Exception {
        // 1. "/api/users/update/" + type으로 requestDto 담아서 put 요청
        MvcResult mvcResult = mockMvc.perform(put("/api/users/update/" + updatingRequestDto.getType())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updatingRequestDto.getRequestDto())))
                .andExpect(status().isOk())
                .andReturn();

        // 2. 응답으로 responseKey 왔는지 확인
        String responseKey = updatingRequestDto.getResponseKey();
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey(responseKey);

        // 3. responseKey의 값이 responseValue인지 확인
        Assertions.assertThat((String) map.get(responseKey)).contains(updatingRequestDto.getResponseValue());
    }
}
