package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.user.EmailUpdateDto;
import com.eskgus.nammunity.web.dto.user.NicknameUpdateDto;
import com.eskgus.nammunity.web.dto.user.PasswordUpdateDto;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Optional;
import java.util.function.Function;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserApiControllerExceptionTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @Autowired
    private UserRepository userRepository;

    private User user;

    private MockHttpServletRequestBuilder requestBuilder;

    @BeforeEach
    public void setUp() {
        this.user = saveUser(1L);
    }

    private User saveUser(Long id) {
        Long userId = testDB.signUp(id, Role.USER);
        return assertOptionalAndGetEntity(userRepository::findById, userId);
    }

    private User assertOptionalAndGetEntity(Function<Long, Optional<User>> finder, Long contentId) {
        return testDB.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void signUpExceptions() throws Exception {
        this.requestBuilder = post("/api/users");

        // 예외 1. username 유효성 검사 탈락
        requestAndAssertSignUpExceptions("username", "username",
                "ID는 영어 소문자로 시작, 숫자 1개 이상 포함, 한글/특수문자/공백 불가능, 8글자 이상 20글자 이하");

        // 예외 2. password 유효성 검사 탈락
        requestAndAssertSignUpExceptions("password", "password",
                "비밀번호는 영어와 숫자 1개 이상 포함, 특수문자 가능, 한글/공백 불가능, 8글자 이상 20글자 이하");

        // 예외 3. confirmPassword 유효성 검사 탈락
        requestAndAssertSignUpExceptions("confirmPassword", "",
                "비밀번호를 확인하세요.");

        // 예외 4. nickname 유효성 검사 탈락
        requestAndAssertSignUpExceptions("nickname", "nickname!",
                "닉네임은 영어/숫자/한글 가능, 특수문자/공백 불가능, 3글자 이상 10글자 이하");

        // 예외 5. email 유효성 검사 탈락
        requestAndAssertSignUpExceptions("email", "email",
                "이메일 형식이 맞지 않습니다.");

        // 예외 6. username 중복
        requestAndAssertSignUpExceptions("username", user.getUsername(),
                "이미 사용 중인 ID입니다.");

        // 예외 7. password != confirmPassword
        requestAndAssertSignUpExceptions("confirmPassword", "비밀번호",
                "비밀번호가 일치하지 않습니다.");

        // 예외 8. nickname 중복
        requestAndAssertSignUpExceptions("nickname", user.getNickname(),
                "이미 사용 중인 닉네임입니다.");

        // 예외 9. email 중복
        requestAndAssertSignUpExceptions("email", user.getEmail(),
                "이미 사용 중인 이메일입니다.");
    }

    private void requestAndAssertSignUpExceptions(String expectedField, String value, String expectedDefaultMessage) throws Exception {
        RegistrationDto requestDto = createRegistrationDto(expectedField, value);
        ResultMatcher[] resultMatchers = mockMvcTestHelper.createResultMatchers(expectedField, expectedDefaultMessage);

        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatchers);
    }

    private RegistrationDto createRegistrationDto(String field, String value) {
        long id = userRepository.count() + 1;
        String username = "username" + id;
        String password = "password" + id;
        String confirmPassword = password;
        String nickname = "nickname" + id;
        String email = "email" + id + "@naver.com";

        switch (field) {
            case "username" -> username = value;
            case "password" -> password = value;
            case "confirmPassword" -> confirmPassword = value;
            case "nickname" -> nickname = value;
            default ->  email = value;
        }

        return RegistrationDto.builder()
                .username(username).password(password).confirmPassword(confirmPassword)
                .nickname(nickname).email(email).build();
    }

    @Test
    public void checkExceptions() throws Exception {
        // 예외 1. username 입력 x
        requestAndAssertCheckExceptions("username", "", "ID를 입력하세요.");

        // 예외 2. username 중복
        requestAndAssertCheckExceptions("username", user.getUsername(), "이미 사용 중인 ID입니다.");

        // 예외 3. nickname 입력 x
        requestAndAssertCheckExceptions("nickname", "", "닉네임을 입력하세요.");

        // 예외 4. nickname 중복
        requestAndAssertCheckExceptions("nickname", user.getNickname(), "이미 사용 중인 닉네임입니다.");

        // 예외 5. email 입력 x
        requestAndAssertCheckExceptions("email", "", "이메일을 입력하세요.");

        // 예외 6. email 중복
        requestAndAssertCheckExceptions("email", user.getEmail(), "이미 사용 중인 이메일입니다.");
    }

    private void requestAndAssertCheckExceptions(String name, String value, String expectedDefaultMessage) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get("/api/users/validation");
        ResultMatcher[] resultMatchers = mockMvcTestHelper.createResultMatchers(name, expectedDefaultMessage);

        mockMvcTestHelper.requestAndAssertStatusIsBadRequestWithParam(requestBuilder, name, value, resultMatchers);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updatePasswordExceptions() throws Exception {
        this.requestBuilder = put("/api/users/password");

        // 예외 1. 현재 비밀번호 입력 x
        requestAndAssertUpdatePasswordExceptions("oldPassword", "",
                "현재 비밀번호를 입력하세요.");

        // 예외 2. 새 비밀번호 유효성 검사 탈락
        requestAndAssertUpdatePasswordExceptions("password", "비밀번호",
                "비밀번호는 영어와 숫자 1개 이상 포함, 특수문자 가능, 한글/공백 불가능, 8글자 이상 20글자 이하");

        // 예외 3. 비밀번호 확인 x
        requestAndAssertUpdatePasswordExceptions("confirmPassword", "",
                "비밀번호를 확인하세요.");

        // 예외 4. 현재 비밀번호 일치 x
        requestAndAssertUpdatePasswordExceptions("oldPassword", "password123",
                "현재 비밀번호가 일치하지 않습니다.");

        // 예외 5. 현재 비밀번호 == 새 비밀번호
        requestAndAssertUpdatePasswordExceptions("password", "password1",
                "현재 비밀번호와 새 비밀번호가 같으면 안 됩니다.");

        // 예외 6. 비밀번호 확인 일치 x
        requestAndAssertUpdatePasswordExceptions("confirmPassword", "password123",
                "비밀번호가 일치하지 않습니다.");
    }

    private void requestAndAssertUpdatePasswordExceptions(String expectedField, String value,
                                                          String expectedDefaultMessage) throws Exception {
        PasswordUpdateDto requestDto = createPasswordUpdateDto(expectedField, value);
        ResultMatcher[] resultMatchers = mockMvcTestHelper.createResultMatchers(expectedField, expectedDefaultMessage);

        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatchers);
    }

    private PasswordUpdateDto createPasswordUpdateDto(String field, String value) {
        String oldPassword = "password1";
        String password = "password2";
        String confirmPassword = password;

        switch (field) {
            case "oldPassword" -> oldPassword = value;
            case "password" -> password = value;
            default -> confirmPassword = value;
        }

        return PasswordUpdateDto.builder()
                .oldPassword(oldPassword).password(password).confirmPassword(confirmPassword).build();
    }

    @Test
    @WithMockUser(username = "username1")
    public void updateNicknameExceptions() throws Exception {
        this.requestBuilder = put("/api/users/nickname");

        // 예외 1. nickname 유효성 검사 탈락
        requestAndAssertUpdateNicknameExceptions("nickname!",
                "닉네임은 영어/숫자/한글 가능, 특수문자/공백 불가능, 3글자 이상 10글자 이하");

        // 예외 2. 현재 닉네임 == 새 닉네임
        requestAndAssertUpdateNicknameExceptions(user.getNickname(), "현재 닉네임과 같습니다.");

        // 예외 3. 닉네임 중복
        User user2 = saveUser(user.getId() + 1);
        requestAndAssertUpdateNicknameExceptions(user2.getNickname(), "이미 사용 중인 닉네임입니다.");
    }

    private void requestAndAssertUpdateNicknameExceptions(String value, String expectedDefaultMessage) throws Exception {
        NicknameUpdateDto requestDto = new NicknameUpdateDto(value);
        ResultMatcher[] resultMatchers = mockMvcTestHelper.createResultMatchers("nickname", expectedDefaultMessage);

        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatchers);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updateEmailExceptions() throws Exception {
        updateUserEnabled(user);

        this.requestBuilder = put("/api/users/email");

        // 예외 1. email 유효성 검사 탈락
        requestAndAssertUpdateEmailExceptions("email", "이메일 형식이 맞지 않습니다.");

        // 예외 2. 현재 이메일 == 새 이메일
        requestAndAssertUpdateEmailExceptions(user.getEmail(), "현재 이메일과 같습니다.");

        // 예외 3. 이메일 중복
        User user2 = saveUser(user.getId() + 1);
        requestAndAssertUpdateEmailExceptions(user2.getEmail(), "이미 사용 중인 이메일입니다.");
    }

    private void updateUserEnabled(User user) {
        user.updateEnabled();
        userRepository.save(user);
    }

    private void requestAndAssertUpdateEmailExceptions(String value, String expectedDefaultMessage) throws Exception {
        EmailUpdateDto requestDto = new EmailUpdateDto(value);
        ResultMatcher[] resultMatchers = mockMvcTestHelper.createResultMatchers("email", expectedDefaultMessage);

        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatchers);
    }
}
