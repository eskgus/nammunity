package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.TestDB;
import com.eskgus.nammunity.domain.reports.ContentReportsRepository;
import com.eskgus.nammunity.domain.user.*;
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

import java.time.Period;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SignInApiControllerExceptionTest {
    @Autowired
    private TestDB testDB;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContentReportsRepository contentReportsRepository;

    @Autowired
    private BannedUsersRepository bannedUsersRepository;

    @BeforeEach
    public void setUp() {
        this.mockMvc = testDB.setUp();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void causeExceptionsOnFindingUsername() throws Exception {
        // 예외 1. 이메일 입력 x
        requestAndAssertForExceptionOnFindingUsername("", "입력");

        // 예외 2. 이메일 형식 x
        requestAndAssertForExceptionOnFindingUsername("email", "형식");

        // 예외 3. 가입되지 x 이메일
        requestAndAssertForExceptionOnFindingUsername("email111@naver.com", "가입되지");
    }

    @Test
    public void causeExceptionsOnFindingPassword() throws Exception {
        // 예외 1. username 입력 x
        requestAndAssertForExceptionOnFindingPassword("", "입력");

        // 예외 2. 존재하지 x username
        String username = "username1";
        requestAndAssertForExceptionOnFindingPassword(username, "존재하지");

        // 예외 3. 활동 정지된 사용자
        // 1-1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();
        User user2 = userRepository.findById(testDB.signUp(2L, Role.USER)).get();
        Assertions.assertThat(userRepository.count()).isEqualTo(2);

        // 1-2. user2가 user1 사용자 신고 * 3
        testDB.saveUserReports(user1, user2);
        Assertions.assertThat(contentReportsRepository.count()).isEqualTo(3);

        // 1-3. user1 활동 정지
        testDB.saveBannedUsers(user1, Period.ofWeeks(1));
        Assertions.assertThat(bannedUsersRepository.count()).isOne();

        // 1-4. 비밀번호 찾기 요청
        requestAndAssertForExceptionOnFindingPassword(username, "활동 정지");
    }

    private void requestAndAssertForExceptionOnFindingUsername(String email, String responseValue) throws Exception {
        // 1. "/api/users/sign-in"으로 parameter email=email 담아서 get 요청
        MvcResult mvcResult = mockMvc.perform(get("/api/users/sign-in")
                        .param("email", email))
                .andExpect(status().isOk())
                .andReturn();

        // 2. 응답으로 "error" 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");

        // 3. "error"의 값이 responseValue인지 확인
        Assertions.assertThat((String) map.get("error")).contains(responseValue);
    }

    private void requestAndAssertForExceptionOnFindingPassword(String username, String responseValue) throws Exception {
        // 1. "/api/users/sign-in"으로 parameter username=username 담아서 put 요청
        MvcResult mvcResult = mockMvc.perform(put("/api/users/sign-in")
                        .param("username", username))
                .andExpect(status().isOk())
                .andReturn();

        // 2. 응답으로 "error" 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");

        // 3. "error"의 값이 responseValue인지 확인
        Assertions.assertThat((String) map.get("error")).contains(responseValue);
    }
}
