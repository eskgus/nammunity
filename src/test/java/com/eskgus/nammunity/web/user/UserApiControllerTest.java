package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.user.EmailUpdateDto;
import com.eskgus.nammunity.web.dto.user.NicknameUpdateDto;
import com.eskgus.nammunity.web.dto.user.PasswordUpdateDto;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Optional;
import java.util.function.Function;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserApiControllerTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    public void check() throws Exception {
        // 1. username
        requestAndAssertCheck("username", "username1");

        // 2. nickname
        requestAndAssertCheck("nickname", "nickname1");

        // 3. email
        requestAndAssertCheck("email", "email1@naver.com");
    }

    private void requestAndAssertCheck(String name, String value) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get("/api/users/validation");

        mockMvcTestHelper.requestAndAssertStatusIsOkWithParam(requestBuilder, name, value);
    }

    @Test
    public void signUp() throws Exception {
        RegistrationDto requestDto = createRegistrationDto();

        MockHttpServletRequestBuilder requestBuilder = post("/api/users");

        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, requestDto);
    }

    private RegistrationDto createRegistrationDto() {
        return RegistrationDto.builder()
                .username("username1").password("password1").confirmPassword("password1")
                .nickname("nickname1").email("email111@naver.com").build();
    }

    @Test
    @WithMockUser(username = "username1")
    public void updatePassword() throws Exception {
        User user = saveUser();

        PasswordUpdateDto requestDto = createPasswordUpdateDto(user);
        MockHttpServletRequestBuilder requestBuilder = put("/api/users/password");

        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, requestDto);
    }

    private User saveUser() {
        Long userId = testDataHelper.signUp(1L, Role.USER);
        return assertOptionalAndGetEntity(userRepository::findById, userId);
    }

    private User assertOptionalAndGetEntity(Function<Long, Optional<User>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }

    private PasswordUpdateDto createPasswordUpdateDto(User user) {
        String newPassword = "password" + (user.getId() + 1);
        return PasswordUpdateDto.builder()
                .oldPassword("password" + user.getId())
                .password(newPassword).confirmPassword(newPassword).build();
    }

    @Test
    @WithMockUser(username = "username1")
    public void updateNickname() throws Exception {
        User user = saveUser();

        NicknameUpdateDto requestDto = createNicknameUpdateDto(user);
        MockHttpServletRequestBuilder requestBuilder = put("/api/users/nickname");

        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, requestDto);
    }

    private NicknameUpdateDto createNicknameUpdateDto(User user) {
        String newNickname = "nickname" + (user.getId() + 1);
        return new NicknameUpdateDto(newNickname);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updateEmail() throws Exception {
        User user = saveUser();
        updateUserEnabled(user);

        EmailUpdateDto requestDto = createEmailUpdateDto(user);
        MockHttpServletRequestBuilder requestBuilder = put("/api/users/email");

        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, requestDto);
    }

    private void updateUserEnabled(User user) {
        user.updateEnabled();
        userRepository.save(user);
    }

    private EmailUpdateDto createEmailUpdateDto(User user) {
        String newEmail = "email" + (user.getId() + 1) + "@naver.com";
        return new EmailUpdateDto(newEmail);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteUser() throws Exception {
        saveUser();

        MockHttpServletRequestBuilder requestBuilder = delete("/api/users");

        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, null);
    }
}
