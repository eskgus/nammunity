package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.user.EmailUpdateDto;
import com.eskgus.nammunity.web.dto.user.NicknameUpdateDto;
import com.eskgus.nammunity.web.dto.user.PasswordUpdateDto;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static com.eskgus.nammunity.domain.enums.Fields.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserApiControllerIntegrationTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @Autowired
    private UserRepository userRepository;

    private static final Long ID = 1L;
    private static final String USERNAME_VALUE = USERNAME.getKey() + ID;
    private static final String PASSWORD_VALUE = PASSWORD.getKey() + ID;
    private static final String NICKNAME_VALUE = NICKNAME.getKey();
    private static final String EMAIL_VALUE = EMAIL.getKey() + "@naver.com";

    private static final String REQUEST_MAPPING = "/api/users";

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    @WithAnonymousUser
    public void signUp() throws Exception {
        // given
        RegistrationDto requestDto = RegistrationDto.builder()
                .username(USERNAME_VALUE).password(PASSWORD_VALUE).confirmPassword(PASSWORD_VALUE)
                .nickname(NICKNAME_VALUE).email(EMAIL_VALUE).build();

        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING);
        performAndExpectOk(requestBuilder, requestDto);
    }

    @Test
    @WithAnonymousUser
    public void checkUsername() throws Exception {
        testCheck(USERNAME, USERNAME_VALUE);
    }

    @Test
    @WithAnonymousUser
    public void checkNickname() throws Exception {
        testCheck(NICKNAME, NICKNAME_VALUE);
    }

    @Test
    @WithAnonymousUser
    public void checkEmail() throws Exception {
        testCheck(EMAIL, EMAIL_VALUE);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updatePassword() throws Exception {
        // given
        saveUser();

        PasswordUpdateDto requestDto = PasswordUpdateDto.builder()
                .oldPassword(PASSWORD_VALUE)
                .password("new" + PASSWORD_VALUE).confirmPassword("new" + PASSWORD_VALUE).build();

        // when/then
        testUpdate(PASSWORD, requestDto);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updateNickname() throws Exception {
        // given
        saveUser();

        NicknameUpdateDto requestDto = new NicknameUpdateDto(NICKNAME_VALUE);

        // when/then
        testUpdate(NICKNAME, requestDto);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updateEmail() throws Exception {
        // given
        saveUser();

        EmailUpdateDto requestDto = new EmailUpdateDto(EMAIL_VALUE);

        // when/then
        testUpdate(EMAIL, requestDto);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteUser() throws Exception {
        // given
        saveUser();

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING);
        ResultMatcher resultMatcher = header().doesNotExist(HttpHeaders.SET_COOKIE);
        mockMvcTestHelper.performAndExpectOkWithCookie(requestBuilder, null, resultMatcher);
    }

    private void saveUser() {
        Long userId = testDataHelper.signUp(1L, Role.USER);
        testDataHelper.assertOptionalAndGetEntity(userRepository::findById, userId);
    }

    private void testCheck(Fields field, String value) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(REQUEST_MAPPING + "/validation");
        mockMvcTestHelper.performAndExpectOkWithParam(requestBuilder, field, value);
    }

    private <Dto> void testUpdate(Fields endpoint, Dto requestDto) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = put(REQUEST_MAPPING + "/" + endpoint.getKey());
        performAndExpectOk(requestBuilder, requestDto);
    }

    private <Dto> void performAndExpectOk(MockHttpServletRequestBuilder requestBuilder, Dto requestDto) throws Exception {
        mockMvcTestHelper.performAndExpectOk(requestBuilder, requestDto);
    }
}
